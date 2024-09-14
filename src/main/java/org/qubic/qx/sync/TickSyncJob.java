package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mio.qubic.computor.crypto.IdentityUtil;
import org.mio.qubic.computor.domain.qx.Qx;
import org.mio.qubic.computor.domain.std.SignedTransaction;
import org.mio.qubic.computor.domain.std.Transaction;
import org.qubic.qx.adapter.qubicj.NodeService;
import org.qubic.qx.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
public class TickSyncJob {

    private final TickRepository tickRepository;
    private final NodeService nodeService;
    private long syncFromTick;
    private long syncToTick;
    private final byte[] qxPublicKey;

    public TickSyncJob(TickRepository tickRepository, NodeService nodeService, IdentityUtil identityUtil) {
        this.tickRepository = tickRepository;
        this.nodeService = nodeService;
        this.qxPublicKey = identityUtil.getPublicKeyFromIdentity(Qx.ADDRESS);
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
                .doOnNext(tick -> log.info("Syncing from tick [{}].", this.syncFromTick))
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
                .doOnNext(processed -> log.info("Processing tick [{}]. Already stored: [{}].", tickNumber, processed))
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber)
                        : queryTickTransactions(tickNumber));
    }

    private Mono<Long> queryTickTransactions(Long tickNumber) {
        return nodeService.getTickTransactions(tickNumber)
                .doFirst(() -> log.info("Tick [{}]: query node.", tickNumber))
                .filter(this::isRelevantTransaction)
                .map(this::processTransaction) // store transaction
                .collectList()
                .flatMap(txs -> storeTick(tickNumber, txs))
                .map(l -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno));
    }

    private Mono<Long> storeTick(Long tickNumber, List<SignedTransaction> txs) {
        if (CollectionUtils.isEmpty(txs)) {
            return tickRepository.addToProcessedTicks(tickNumber)
                    .then(Mono.just(0L));
        } else {
            return tickRepository.addToProcessedTicks(tickNumber)
                    .then(tickRepository.setTickTransactions(tickNumber, txs.stream().map(SignedTransaction::getTransactionHash).toList())
                    .doOnNext(count -> log.info("Stored [{}] transactions for tick [{}].", count, tickNumber)));
        }
    }

    private SignedTransaction processTransaction(SignedTransaction signedTransaction) {
        Transaction transaction = signedTransaction.getTransaction();
        log.info("Processing qx transaction [{}] in tick [{}] with input type [{}].",
                signedTransaction.getTransactionHash(),
                transaction.getTick(),
                transaction.getInputType());
        return signedTransaction;
    }

    private boolean isRelevantTransaction(SignedTransaction stx) {
        Transaction transaction = stx.getTransaction();
        return Objects.deepEquals(transaction.getDestinationPublicKey(), qxPublicKey)
                && (transaction.getInputType() == Qx.Procedure.QX_ADD_ASK_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_REMOVE_ASK_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_ADD_BID_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_REMOVE_BID_ORDER.getCode());
    }

    private Mono<Long> getLowestAvailableTick() {
        return nodeService.getInitialTick(); // TODO replace with integration layer
    }

}
