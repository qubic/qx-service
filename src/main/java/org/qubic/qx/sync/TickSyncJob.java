package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Slf4j
public class TickSyncJob {

    private final TickRepository tickRepository;
    private final TransactionRepository transactionRepository;
    private final CoreApiService coreService;
    private final TransactionProcessor transactionProcessor;

    public TickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository, CoreApiService coreService, TransactionProcessor transactionProcessor) {
        this.tickRepository = tickRepository;
        this.transactionRepository = transactionRepository;
        this.coreService = coreService;
        this.transactionProcessor = transactionProcessor;
    }

    public Flux<Long> sync(long currentTick) {
        // get highes available start block (start from scratch or last db state)
        return getLowestAvailableTick()
                .flatMap(this::calculateStartTick)
                .flatMapMany(calculateSyncRange(currentTick))
                .doOnNext(tickNumber -> log.debug("Preparing to sync tick [{}].", tickNumber))
                .concatMap(this::processTick) // sequential processing for order book calculations
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
            return storeTickNumberMono.
                    then(Mono.just(false));
        } else {


            // TODO store qx tick information if there are transactions, like time for displaying order data later

            return transactionProcessor.processQxOrders(tickNumber, txs)
                    .doFirst(() -> log.info("Tick [{}]: processing [{}] qx orders.", tickNumber, txs.size()))
                    .thenMany(storeTransactions(txs))
                    .then(storeTickNumberMono)
                    .then(tickRepository.addToQxTicks(tickNumber))

                    // TODO .then(tickRepository.storeTick) - at least we need the timestamp
                    .then(tickRepository.setTickTransactions(tickNumber, txs.stream().map(Transaction::transactionHash).toList()));
        }
    }

    private Flux<Transaction> storeTransactions(List<Transaction> txs) {
        return Flux.fromIterable(txs)
                .flatMap(transactionRepository::putTransaction);
    }

    // minor helper methods

    private static Function<Long, Publisher<? extends Long>> calculateSyncRange(long targetTick) {
        return fromTick -> {
            long syncToTick = Math.max(targetTick, fromTick);
            int numberOfTicks = (int) (syncToTick - fromTick);
            log.info("Syncing from tick [{}] to [{}]. Number of ticks: [{}].", fromTick, syncToTick, numberOfTicks);
            return Flux.range(0, numberOfTicks).map(counter -> fromTick + counter);
        };
    }

    public Mono<Boolean> updateLatestSyncedTick(long syncedTick) {
       return tickRepository.getLatestSyncedTick()
               .flatMap(latest -> latest >= syncedTick
                       ? Mono.just(false)
                       : tickRepository.setLatestSyncedTick(syncedTick));
    }

    private Mono<Long> calculateStartTick(Long lowestTick) {
        return tickRepository.getLatestSyncedTick()
                .map(latestStoredTick -> lowestTick > latestStoredTick
                        ? lowestTick
                        : latestStoredTick);
    }

    private Mono<Long> getLowestAvailableTick() {
        return coreService.getInitialTick();
    }

    public Mono<Long> getCurrentTick() {
        return coreService.getCurrentTick();
    }

}
