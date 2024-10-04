package org.qubic.qx.adapter.qubicj;

import at.qubic.api.domain.std.SignedTransaction;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.extern.slf4j.Slf4j;
import at.qubic.api.service.ComputorService;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx;
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

    private static final int RETRIES = 3;
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
                .filter(ti -> ti.getTick() > 0) // TODO fix error handling in qubicj - return empty instead of 'empty' tick info
                .map(ti -> Integer.toUnsignedLong(ti.getTick()))
                .repeatWhenEmpty(RETRIES, repeat -> repeat.doOnNext(count -> log.info("Repeat [{}] getting current tick.", count)))
                .retryWhen(Retry.max(RETRIES).doBeforeRetry(rs -> log.info("Retry [{}] getting current tick.", rs.totalRetries())))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get current tick.")));
    }

    @Override
    public Mono<Long> getInitialTick() {
        return computorService.getCurrentTickInfo()
                .map(ti -> Integer.toUnsignedLong(ti.getInitialTick()))
                .doOnNext(tickNumber -> log.debug("Initial tick number: {}", tickNumber))
                .repeatWhenEmpty(RETRIES, repeat -> repeat.doOnNext(count -> log.info("Repeat [{}] getting initial tick.", count)))
                .retryWhen(Retry.max(RETRIES).doBeforeRetry(rs -> log.info("Retry [{}] getting initial tick.", rs.totalRetries())))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get initial tick.")));
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return computorService.getTickTransactions((int) tick)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .retryWhen(Retry.backoff(RETRIES, Duration.ofSeconds(1)))
                .doOnError(t -> log.error("Failed to get transactions for tick: [{}].", tick))
                .switchIfEmpty( // log a warning. That shouldn't happen often.
                        Mono.just(String.format("Received no transactions for tick [%d].", tick))
                                .doOnNext(log::warn)
                                .thenMany(Flux.empty())
                )
                .filter(this::isRelevantTransaction)
                .doOnNext(tx -> log.debug("Qx transaction: {}", tx))
                .map(transactionMapper::map);
    }

    private boolean isRelevantTransaction(SignedTransaction stx) {
        at.qubic.api.domain.std.Transaction transaction = stx.getTransaction();
        if (isRelevantInputType(transaction) && isSentToQxAddress(transaction)) {
            log.debug("[{}]: [{}].", Qx.Order.fromCode(transaction.getInputType()), stx.getTransactionHash());
            return true;
        } else {
            return false;
        }
    }

    private static boolean isRelevantInputType(at.qubic.api.domain.std.Transaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains((int) transaction.getInputType());
    }

    private static boolean isSentToQxAddress(at.qubic.api.domain.std.Transaction transaction) {
        return Objects.deepEquals(transaction.getDestinationPublicKey(), Qx.QX_PUBLIC_KEY);
    }

}
