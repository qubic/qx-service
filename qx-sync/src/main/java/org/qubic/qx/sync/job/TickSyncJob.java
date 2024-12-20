package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.assets.AssetService;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public class TickSyncJob {

    private final AssetService assetService;
    private final TickRepository tickRepository;
    private final CoreApiService coreService;
    private final EventApiService eventService;
    private final TransactionProcessor transactionProcessor;

    public TickSyncJob(AssetService assetService, TickRepository tickRepository, CoreApiService coreService, EventApiService eventService, TransactionProcessor transactionProcessor) {
        this.assetService = assetService;
        this.tickRepository = tickRepository;
        this.coreService = coreService;
        this.eventService = eventService;
        this.transactionProcessor = transactionProcessor;
    }

    public Mono<Long> sync() {
        // get highes available start block (start from scratch or last db state)
        return getLatestAvailableTick()
                .flatMap(this::calculateStartAndEndTick)
                .flatMapMany(this::calculateSyncRange)
                .doOnNext(tickNumber -> log.debug("Syncing tick [{}].", tickNumber))
                .concatMap(this::processTick)
                .last()
                .onErrorResume(NoSuchElementException.class, e -> Mono.empty());
    }

    private Mono<Tuple2<TickInfo, EpochAndTick>> getLatestAvailableTick() {
        return Mono.zip(coreService.getTickInfo(), eventService.getLastProcessedTick())
                .doOnNext(tuple -> {
                    if (tuple.getT1().tick() > tuple.getT2().tickNumber() + 2) {
                        log.info("Current tick: [{}]. Events are available until tick [{}].",
                                tuple.getT1().tick(), tuple.getT2().tickNumber());
                    }
                });
    }

    private Mono<Long> processTick(Long tickNumber) {
        return tickRepository.isProcessedTick(tickNumber)
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber).doOnNext(n -> log.debug("Skipping already stored tick [{}].", n))
                        : processNewTick(tickNumber));
    }

    private Mono<Long> processNewTick(Long tickNumber) {
        return coreService.getQxTransactions(tickNumber)
                .doOnNext(tx -> log.info("[{}] Received [{}] transaction.", tx.transactionHash(), Qx.OrderType.fromCode(tx.inputType())))
                .collectList()
                .flatMap(list -> processTransactions(tickNumber, list))
                .map(x -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno))
                .doOnError(err -> log.error("Error processing tick [{}]: {}", tickNumber, err.toString()));
    }

    public Mono<Void> initializeOrderBooks() {
        return coreService.getCurrentTick()
                .flatMapMany(assetService::initializeOrderBooks)
                .then();
    }

    private Mono<Boolean> processTransactions(Long tickNumber, List<Transaction> txs) {
        Mono<Long> storeTickNumberMono = Mono.defer(() -> tickRepository.addToProcessedTicks(tickNumber));
        if (CollectionUtils.isEmpty(txs)) {
            return storeTickNumberMono.then(Mono.just(false));
        } else {
            Mono<List<TransactionEvents>> eventsMono = eventService.getTickEvents(tickNumber);
            Mono<TickData> tickDataMono = coreService.getTickData(tickNumber);
            return Mono.zip(eventsMono, tickDataMono)
                    .switchIfEmpty(Mono.error(new EmptyResultException(String.format("Could not get events or tick data for tick [%s].", tickNumber))))
                    .doFirst(() -> log.info("Tick [{}]: processing [{}] qx orders.", tickNumber, txs.size()))
                    .flatMap(tuple -> transactionProcessor.processQxTransactions(tickNumber,
                            tuple.getT2().timestamp(), // tick data
                            tuple.getT1(), // transaction events
                            txs))
                    .then(storeTickNumberMono)
                    .then(Mono.just(true));

        }
    }

    // minor helper methods

    private Flux<Long> calculateSyncRange(Tuple2<Long, Long> startAndEndTick) {
        long startTick = startAndEndTick.getT1();
        long endTick = startAndEndTick.getT2();
        int numberOfTicks = (int) (endTick - startTick); // we don't sync the latest tick (integration api might still be behind)
        if (numberOfTicks > 0) {
            if (numberOfTicks > 1) {
                log.info("Syncing from tick [{}] (incl) to [{}] (excl). Number of ticks: [{}].", startTick, endTick, numberOfTicks);
                return Flux.range(0, numberOfTicks).map(counter -> startTick + counter);
            } else {
                return Flux.just(startTick);
            }
        } else {
            log.debug("Nothing to sync...");
            return Flux.empty();
        }
    }

    public Mono<Long> updateLatestSyncedTick(long syncedTick) {
       return tickRepository.getLatestSyncedTick()
               .flatMap(latest -> latest >= syncedTick
                       ? Mono.just(false)
                       : tickRepository.setLatestSyncedTick(syncedTick))
               .then(Mono.just(syncedTick));
    }

    private Mono<Tuple2<Long, Long>> calculateStartAndEndTick(Tuple2<TickInfo, EpochAndTick> tuple) {
        return tickRepository.getLatestSyncedTick()
                .map(latestStoredTick -> latestStoredTick < tuple.getT1().initialTick()
                        ? tuple.getT1().initialTick()
                        : latestStoredTick + 1)
                // take the lowest common tick where event data is available (most probably always getT2().tickNumber()
                // +1 because end tick is not inclusive by default
                .map(startTick -> Tuples.of(startTick, Math.min(tuple.getT1().tick(), tuple.getT2().tickNumber()+1)));
    }

}
