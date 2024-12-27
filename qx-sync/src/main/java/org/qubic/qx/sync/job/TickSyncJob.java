package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.job.domain.TransactionWithEvents;
import org.qubic.qx.sync.mapper.TransactionMapper;
import org.qubic.qx.sync.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class TickSyncJob {

    private final TickRepository tickRepository;
    private final CoreApiService coreService;
    private final EventApiService eventService;
    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private final TransactionProcessor transactionProcessor;

    public TickSyncJob(TickRepository tickRepository, CoreApiService coreService, EventApiService eventService, TransactionProcessor transactionProcessor) {
        this.tickRepository = tickRepository;
        this.coreService = coreService;
        this.eventService = eventService;
        this.transactionProcessor = transactionProcessor;
    }

    public Flux<Long> sync() {
        // get highes available start block (start from scratch or last db state)
        return getLatestAvailableTick()
                .flatMap(this::calculateStartAndEndTick)
                .flatMapMany(this::calculateSyncRange)
                .doOnNext(tickNumber -> log.debug("Syncing tick [{}].", tickNumber))
                .concatMap(this::processTick);
    }

    public Mono<Long> updateLatestSyncedTick(long syncedTick) {
        return tickRepository.getLatestSyncedTick()
                .flatMap(latest -> latest >= syncedTick
                        ? Mono.just(false)
                        : tickRepository.setLatestSyncedTick(syncedTick))
                .then(Mono.just(syncedTick));
    }

    private Mono<Tuple2<TickInfo, EpochAndTick>> getLatestAvailableTick() {
        return Mono.zip(coreService.getTickInfo(), eventService.getLastProcessedTick())
                .doOnNext(tuple -> {
                    // log if there is a 'larger' gap between current tick and event service
                    if (Math.abs(tuple.getT1().tick() - tuple.getT2().tickNumber()) > 3) {
                        log.info("Current tick: [{}]. Events are available until tick [{}].",
                                tuple.getT1().tick(), tuple.getT2().tickNumber());
                    }
                });
    }

    private Flux<Long> calculateSyncRange(Tuple2<Long, Long> startAndEndTick) {
        long startTick = startAndEndTick.getT1();
        long endTick = startAndEndTick.getT2(); // we could do +1 here because end tick is exclusive but we better wait one tick
        int numberOfTicks = (int) (endTick - startTick); // we don't sync the latest tick (integration api might still be behind)
        if (numberOfTicks > 0) {
            if (numberOfTicks > 1) {
                log.info("Syncing from tick [{}] (incl) to [{}] (excl). Number of ticks: [{}].", startTick, endTick, numberOfTicks);
                return Flux.range(0, numberOfTicks).map(counter -> startTick + counter);
            } else {
                return Flux.just(startTick);
            }
        } else {
            log.debug("Nothing to sync... start [{}], end [{}]", startTick, endTick);
            return Flux.empty();
        }
    }

    private Mono<Tuple2<Long, Long>> calculateStartAndEndTick(Tuple2<TickInfo, EpochAndTick> tuple) {
        return tickRepository.getLatestSyncedTick()
                .map(latestStoredTick -> latestStoredTick < tuple.getT1().initialTick()
                        ? tuple.getT1().initialTick()
                        : latestStoredTick + 1)
                // take the lowest common tick where event data is available (most probably always getT2().tickNumber()
                .map(startTick -> Tuples.of(startTick, Math.min(tuple.getT1().tick(), tuple.getT2().tickNumber())));
    }

    private Mono<Long> processTick(Long tickNumber) {
        return tickRepository.isProcessedTick(tickNumber)
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber).doOnNext(n -> log.debug("Skipping already stored tick [{}].", n))
                        : processNewTick(tickNumber));
    }

    private Mono<Long> processNewTick(Long tickNumber) {
        Mono<List<TransactionEvents>> tickEventsMono = eventService.getTickEvents(tickNumber); // never empty
        Mono<TickData> tickDataMono = coreService.getTickData(tickNumber); // never empty
        Mono<List<Transaction>> qxTransactionsListMono = coreService.getQxTransactions(tickNumber)
                .doOnNext(tx -> log.info("[{}] Received [{}] transaction.", tx.transactionHash(), Qx.OrderType.fromCode(tx.inputType())))
                .collectList(); // empyt list if empty flux

        return Mono.zip(tickDataMono, qxTransactionsListMono)
                .switchIfEmpty(Mono.error(new EmptyResultException(String.format("Could not get tick transactions or data for tick [%s].", tickNumber))))
                .map(tuple -> tuple.getT2().stream().map(tx -> transactionMapper.map(tx, tuple.getT1().timestamp())))
                .zipWith(tickEventsMono)
                .switchIfEmpty(Mono.error(new EmptyResultException(String.format("Could not get tick events for tick [%s].", tickNumber))))
                .map(tuple -> tuple.getT1().map(tx -> new TransactionWithEvents(tx, getEventsForTransaction(tuple, tx))).toList())
                .flatMap(list -> processTransactions(tickNumber, list))
                .map(x -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno))
                .doOnError(err -> log.error("Error processing tick [{}]: {}", tickNumber, err.toString()));
    }

    private static List<TransactionEvent> getEventsForTransaction(Tuple2<Stream<TransactionWithTime>, List<TransactionEvents>> tuple, TransactionWithTime tx) {
        return tuple.getT2()
                .stream()
                .filter(txe -> StringUtils.equals(tx.transactionHash(), txe.txId()))
                .findAny() // there should only be one
                .orElse(new TransactionEvents(tx.transactionHash(), List.of())) // empty
                .events();
    }

    private Mono<?> processTransactions(Long tickNumber, List<TransactionWithEvents> txs) {
        Mono<Long> storeTickNumberMono = Mono.defer(() -> tickRepository.addToProcessedTicks(tickNumber));
        if (CollectionUtils.isEmpty(txs)) {
            return storeTickNumberMono
                    .then(Mono.just(false));
        } else {
            return Flux.fromIterable(txs)
                    .flatMap(tx -> transactionProcessor.processQxTransaction(tx.transaction(), tx.events()))
                    .then(storeTickNumberMono);
        }
    }

//    private Mono<?> processTransaction(List<TransactionWithEvents> transactionsWithEvents) {
//
//        for (TransactionWithEvents transactionWithEvent : transactionsWithEvents) {
//
//            TransactionWithTime transaction = transactionWithEvent.transaction();
//
//            if (transaction.extraData() instanceof QxAssetOrderData) {
//
//            } else if (transaction.extraData() instanceof QxTransferAssetData) {
//
//            } else if (transaction.extraData() instanceof QxIssueAssetData) {
//
//            } else {
//
//            }
//
//
//        }
//
//
//    }

//    private Mono<Boolean> processTransactionsOLD(Long tickNumber, List<Transaction> txs) {
//        Mono<Long> storeTickNumberMono = Mono.defer(() -> tickRepository.addToProcessedTicks(tickNumber));
//        if (CollectionUtils.isEmpty(txs)) {
//            return storeTickNumberMono.then(Mono.just(false));
//        } else {
//            Mono<List<TransactionEvents>> eventsMono = eventService.getTickEvents(tickNumber); // TODO move up
//            Mono<TickData> tickDataMono = coreService.getTickData(tickNumber); // TODO move up
//            return Mono.zip(eventsMono, tickDataMono)
//                    .switchIfEmpty(Mono.error(new EmptyResultException(String.format("Could not get events or tick data for tick [%s].", tickNumber))))
//                    .doFirst(() -> log.info("Tick [{}]: processing [{}] qx orders.", tickNumber, txs.size()))
//                    .flatMap(tuple -> transactionProcessor.processQxTransactions(tickNumber,
//                            tuple.getT2().timestamp(), // tick data
//                            tuple.getT1(), // transaction events
//                            txs))
//                    .then(storeTickNumberMono)
//                    .then(Mono.just(true));
//
//        }
//    }

    // minor helper methods



}
