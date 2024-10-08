package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx;
import org.qubic.qx.domain.TickData;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

@Slf4j
public class TickSyncJob {

    private final TickRepository tickRepository;
    private final TransactionRepository transactionRepository;
    private final CoreApiService coreService;
    private final TransactionProcessor transactionProcessor;

    private TickInfo currentTickInfo;

    public TickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository, CoreApiService coreService, TransactionProcessor transactionProcessor) {
        this.tickRepository = tickRepository;
        this.transactionRepository = transactionRepository;
        this.coreService = coreService;
        this.transactionProcessor = transactionProcessor;
    }

    public Mono<TickInfo> sync() {
        // get highes available start block (start from scratch or last db state)
        return coreService.getTickInfo()
                .doOnNext(ti -> this.currentTickInfo = ti)
                .flatMap(this::calculateStartTick)
                .flatMapMany(this::calculateSyncRange)
                .doOnNext(tickNumber -> log.debug("Preparing to sync tick [{}].", tickNumber))
                .concatMap(this::processTick)
                .then(Mono.defer(() -> Mono.just(currentTickInfo))) // sequential processing for order book calculations
                ;
    }

    private Mono<Long> processTick(Long tickNumber) {
        return tickRepository.isProcessedTick(tickNumber)
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber).doOnNext(n -> log.debug("Skipping already stored tick [{}].", n))
                        : processNewTick(tickNumber));
    }

    private Mono<Long> processNewTick(Long tickNumber) {
        return coreService.getQxTransactions(tickNumber)
                .doFirst(() -> log.debug("Query node for tick [{}].", tickNumber))
                .doOnNext(tx -> log.info("[{}] Received [{}] transaction.", tx.transactionHash(), Qx.OrderType.fromCode(tx.inputType())))
                .collectList()
                .flatMap(list -> processTransactions(tickNumber, list))
                .map(x -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno));
    }

    public Mono<Void> updateAllOrderBooks() {
        return transactionProcessor.updateAllOrderBooks();
    }

    private Mono<Boolean> processTransactions(Long tickNumber, List<Transaction> txs) {
        Mono<Long> storeTickNumberMono = tickRepository.addToProcessedTicks(tickNumber);
        if (CollectionUtils.isEmpty(txs)) {
            return storeTickNumberMono.then(Mono.just(false));
        } else {

            return coreService.getTickData(tickNumber)
                    .doFirst(() -> log.info("Tick [{}]: processing [{}] qx orders.", tickNumber, txs.size()))
                    .map(TickData::timestamp)
                    .flatMap(instant -> transactionProcessor.processQxOrders(tickNumber, instant, txs))
                    .thenMany(storeTransactions(txs))
                    .then(storeTickNumberMono)
                    .then(tickRepository.addToQxTicks(tickNumber))
                    .then(tickRepository.setTickTransactions(tickNumber, txs.stream().map(Transaction::transactionHash).toList()));
        }
    }

    private Flux<Transaction> storeTransactions(List<Transaction> txs) {
        return Flux.fromIterable(txs)
                .flatMap(transactionRepository::putTransaction);
    }

    // minor helper methods

    private Flux<Long> calculateSyncRange(Tuple2<TickInfo, Long> endAndStartTick) {
        long startTick = endAndStartTick.getT2();
        long endTick = Math.max(startTick, endAndStartTick.getT1().tick());
        int numberOfTicks = (int) (endTick - startTick);
        if (numberOfTicks > 1) {
            log.info("Syncing from tick [{}] to [{}]. Number of ticks: [{}].", startTick, endTick, numberOfTicks);
        }
        return Flux.range(0, numberOfTicks)
                .map(counter -> startTick + counter);
    }

    public Mono<Long> updateLatestSyncedTick(long syncedTick) {
       return tickRepository.getLatestSyncedTick()
               .flatMap(latest -> latest >= syncedTick
                       ? Mono.just(false)
                       : tickRepository.setLatestSyncedTick(syncedTick))
               .then(Mono.just(syncedTick));
    }

    private Mono<Tuple2<TickInfo, Long>> calculateStartTick(TickInfo tickInfo) {
        return tickRepository.getLatestSyncedTick()
                .map(latestStoredTick -> tickInfo.initialTick() > latestStoredTick
                        ? tickInfo.initialTick()
                        : latestStoredTick)
                .map(startTick -> Tuples.of(tickInfo, startTick));
    }

}
