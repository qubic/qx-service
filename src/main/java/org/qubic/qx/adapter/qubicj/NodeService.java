package org.qubic.qx.adapter.qubicj;

import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.std.SignedTransaction;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.extern.slf4j.Slf4j;
import at.qubic.api.service.ComputorService;
import org.qubic.qx.adapter.exception.EmptyResultException;
import org.qubic.qx.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class NodeService {

    public static final byte[] QX_PUBLIC_KEY = new byte[] {
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    private final ComputorService computorService;
    private final TransactionMapper transactionMapper;


    private final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(5)
            .build();
    private final Bulkhead bulkhead = Bulkhead.of("getTickTransactionsBulkhead", bulkheadConfig);

    public NodeService(ComputorService computorService, TransactionMapper transactionMapper) {
        this.computorService = computorService;
        this.transactionMapper = transactionMapper;
    }

    public Mono<Long> getCurrentTick() {
        return computorService.getCurrentTickInfo()
                .filter(ti -> ti.getTick() > 0) // TODO fix error handling in qubicj
                .map(ti -> Integer.toUnsignedLong(ti.getTick()))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get current tick.")));
    }

    public Mono<Long> getInitialTick() {
        return computorService.getCurrentTickInfo()
                .map(ti -> Integer.toUnsignedLong(ti.getInitialTick()))
                .doOnNext(tickNumber -> log.debug("Initial tick number: {}", tickNumber))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get initial tick.")));
    }

    public Flux<Transaction> getQxTransactions(long tick) {
        return computorService.getTickTransactions((int) tick)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                .doOnError(t -> log.error("Failed to get transactions for tick: [{}].", tick))
                .filter(this::isRelevantTransaction)
                .doOnNext(tx -> log.debug("Qx transaction: {}", tx))
                .map(transactionMapper::map)
                ;
    }

    private boolean isRelevantTransaction(SignedTransaction stx) {
        at.qubic.api.domain.std.Transaction transaction = stx.getTransaction();
        return Objects.deepEquals(transaction.getDestinationPublicKey(), QX_PUBLIC_KEY)
                && (transaction.getInputType() == Qx.Procedure.QX_ADD_ASK_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_REMOVE_ASK_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_ADD_BID_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_REMOVE_BID_ORDER.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_TRANSFER_SHARE.getCode()
                || transaction.getInputType() == Qx.Procedure.QX_ISSUE_ASSET.getCode()
        );
    }

}
