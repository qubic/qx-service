package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.adapter.qubicj.NodeService;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class TickSyncJob {

    private final TickRepository tickRepository;
    private final TransactionRepository transactionRepository;
    private final NodeService nodeService;
    private long syncFromTick;
    private long syncToTick;

    public TickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository, NodeService nodeService) {
        this.tickRepository = tickRepository;
        this.transactionRepository = transactionRepository;
        this.nodeService = nodeService;
    }

    public Flux<Long> sync(long targetTick) {
        // get highes available start block (start from scratch or last db state)
        return getLowestAvailableTick()
                .flatMap(lowestTick -> tickRepository.getLatestSyncedTick()
                        .map(latestStoredTick -> lowestTick > latestStoredTick
                                ? lowestTick
                                : latestStoredTick))
                // start from latest stored or lowest available tick
                .doOnNext(tick -> this.syncFromTick = tick)
                .doOnNext(tick -> log.debug("Syncing from tick [{}].", this.syncFromTick))
//                .then(getCurrentTick())
                .flatMapMany(fromTick -> {
                    this.syncToTick = Math.max(targetTick, syncFromTick);
                    int numberOfTicks = (int) (syncToTick - syncFromTick);
                    log.info("Syncing from tick [{}] to [{}].", this.syncFromTick, syncToTick);
                    return Flux.range(0, numberOfTicks)
                            .map(counter -> syncFromTick + counter);
                })
                .doOnNext(tickNumber -> log.debug("Preparing to sync tick [{}].", tickNumber))
                .flatMap(this::syncTick) // concat map would process sequential and in order
                ;
    }

    public Mono<Boolean> updateLatestSyncedTick(long syncedTick) {
       return tickRepository.getLatestSyncedTick()
               .flatMap(
                       latest -> latest >= syncedTick ? Mono.just(false) : tickRepository.setLatestSyncedTick(syncedTick)
               );
    }

    public Mono<Long> getCurrentTick() {
        return nodeService.getCurrentTick();
    }

    private Mono<Long> syncTick(Long tickNumber) {
        return tickRepository.isProcessedTick(tickNumber)
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber).doOnNext(n -> log.debug("Skipping already stored tick [{}].", n))
                        : queryTickTransactions(tickNumber));
    }

    private Mono<Long> queryTickTransactions(Long tickNumber) {
        return nodeService.getQxTransactions(tickNumber)
                .doFirst(() -> log.debug("Query node for tick [{}].", tickNumber))
                .flatMap(this::processTransaction)
                .collectList()
                .flatMap(list -> processTickTransactions(tickNumber, list))
                .map(x -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno));
    }

    private Mono<Transaction> processTransaction(Transaction transaction) {
        return Mono.just(transaction)
                .doOnNext(tx -> log.info("Processing transaction [{}] from tick [{}] with input type [{}].",
                        tx.transactionHash(), tx.tick(), tx.inputType()))
                .flatMap(transactionRepository::putTransaction)
                //.flatMap(tx -> tickRepository.addTickTransaction(tx.tick(), tx.transactionHash()))
                .map(b -> transaction);

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

    private Mono<Long> getLowestAvailableTick() {
        return nodeService.getInitialTick(); // TODO replace with integration layer
    }

}
