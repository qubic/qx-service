package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.adapter.qubicj.NodeService;
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
    private final NodeService nodeService;

    public TickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository, NodeService nodeService) {
        this.tickRepository = tickRepository;
        this.transactionRepository = transactionRepository;
        this.nodeService = nodeService;
    }

    public Flux<Long> sync(long targetTick) {
        // get highes available start block (start from scratch or last db state)
        return getLowestAvailableTick()
                .flatMap(this::calculateStartTick)
                .flatMapMany(calculateSyncRange(targetTick))
                .doOnNext(tickNumber -> log.debug("Preparing to sync tick [{}].", tickNumber))
                .concatMap(this::syncTick) // sequential processing for order book calculations
                // .flatMapSequential(this::syncTick) // concat map would process sequential and in order
                ;
    }

    private Mono<Long> syncTick(Long tickNumber) {
        return tickRepository.isProcessedTick(tickNumber)
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber).doOnNext(n -> log.debug("Skipping already stored tick [{}].", n))
                        : processTick(tickNumber));
    }

    private Mono<Long> processTick(Long tickNumber) {
        return nodeService.getQxTransactions(tickNumber)
                .doFirst(() -> log.debug("Query node for tick [{}].", tickNumber))
                .flatMap(this::processTransaction)
                .collectList()
                .flatMap(list -> processTickTransactions(tickNumber, list)) // TODO move transaction processing into here
                .map(x -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno));
    }

    private Mono<Long> processTickTransactions(Long tickNumber, List<Transaction> txs) {
        Mono<Long> storeTickNumberMono = tickRepository.addToProcessedTicks(tickNumber);
        if (CollectionUtils.isEmpty(txs)) {
            return storeTickNumberMono.
                    then(Mono.just(0L));
        } else {
            return storeTickNumberMono
                    .then(tickRepository.addToQxTicks(tickNumber))
                    .then(tickRepository.setTickTransactions(tickNumber, txs.stream().map(Transaction::transactionHash).toList()));
        }
    }

    private Mono<Transaction> processTransaction(Transaction transaction) {
        return Mono.just(transaction)
                .doOnNext(tx -> log.info("Processing transaction [{}] from tick [{}] with input type [{}].",
                        tx.transactionHash(), tx.tick(), tx.inputType()))
                .flatMap(transactionRepository::putTransaction)
                .map(b -> transaction);

    }

    // minor helper methods

    private static Function<Long, Publisher<? extends Long>> calculateSyncRange(long targetTick) {
        return fromTick -> {
            long syncToTick = Math.max(targetTick, fromTick);
            int numberOfTicks = (int) (syncToTick - fromTick);
            log.info("Syncing from tick [{}] to [{}].", fromTick, syncToTick);
            return Flux.range(0, numberOfTicks).map(counter -> fromTick + counter);
        };
    }

    public Mono<Boolean> updateLatestSyncedTick(long syncedTick) {
       return tickRepository.getLatestSyncedTick()
               .flatMap(
                       latest -> latest >= syncedTick ? Mono.just(false) : tickRepository.setLatestSyncedTick(syncedTick)
               );
    }

    private Mono<Long> calculateStartTick(Long lowestTick) {
        return tickRepository.getLatestSyncedTick()
                .map(latestStoredTick -> lowestTick > latestStoredTick
                        ? lowestTick
                        : latestStoredTick);
    }

    private Mono<Long> getLowestAvailableTick() {
        return nodeService.getInitialTick();
    }

    public Mono<Long> getCurrentTick() {
        return nodeService.getCurrentTick();
    }

}
