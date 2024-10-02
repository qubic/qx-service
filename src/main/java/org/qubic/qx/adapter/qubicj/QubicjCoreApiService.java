package org.qubic.qx.adapter.qubicj;

import at.qubic.api.domain.std.SignedTransaction;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import at.qubic.api.service.ComputorService;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.QxSpecs;
import org.qubic.qx.adapter.exception.EmptyResultException;
import org.qubic.qx.adapter.qubicj.mapping.QubicjTransactionMapper;
import org.qubic.qx.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class QubicjCoreApiService implements CoreApiService {

    private final ComputorService computorService;
    private final QubicjTransactionMapper transactionMapper;


    private final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(5)
            .build();
    private final Bulkhead bulkhead = Bulkhead.of("getTickTransactionsBulkhead", bulkheadConfig);

    public QubicjCoreApiService(ComputorService computorService, QubicjTransactionMapper transactionMapper) {
        this.computorService = computorService;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Mono<Long> getCurrentTick() {
        return computorService.getCurrentTickInfo()
                .filter(ti -> ti.getTick() > 0) // TODO fix error handling in qubicj
                .map(ti -> Integer.toUnsignedLong(ti.getTick()))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get current tick.")));
    }

    @Override
    public Mono<Long> getInitialTick() {
        return computorService.getCurrentTickInfo()
                .map(ti -> Integer.toUnsignedLong(ti.getInitialTick()))
                .doOnNext(tickNumber -> log.debug("Initial tick number: {}", tickNumber))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get initial tick.")));
    }

    @Override
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
        String relevantQxOperation = QxSpecs.INPUT_TYPES.get((int) transaction.getInputType());
        if (Objects.deepEquals(transaction.getDestinationPublicKey(), QxSpecs.QX_PUBLIC_KEY)
                && StringUtils.isNotBlank(relevantQxOperation)) {
            log.debug("[{}]: [{}].", relevantQxOperation, stx.getTransactionHash());
            return true;
        } else {
            return false;
        }
    }

}
