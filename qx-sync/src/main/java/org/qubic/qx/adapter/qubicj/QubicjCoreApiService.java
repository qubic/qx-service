package org.qubic.qx.adapter.qubicj;

import at.qubic.api.domain.std.SignedTransaction;
import at.qubic.api.service.ComputorService;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx;
import org.qubic.qx.adapter.exception.EmptyResultException;
import org.qubic.qx.adapter.qubicj.mapping.QubicjMapper;
import org.qubic.qx.domain.TickData;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.TickTransactionsStatus;
import org.qubic.qx.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class QubicjCoreApiService implements CoreApiService {

    private static final int RETRIES = 3;
    private final ComputorService computorService;
    private final QubicjMapper mapper;


    private final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(2)
            .build();
    private final Bulkhead bulkhead = Bulkhead.of("getTickTransactionsBulkhead", bulkheadConfig);

    public QubicjCoreApiService(ComputorService computorService, QubicjMapper mapper) {
        this.computorService = computorService;
        this.mapper = mapper;
    }

    @Override
    public Mono<TickTransactionsStatus> getTickTransactionsStatus(long tick) {
        log.debug("Getting tick transactions status not implemented for qubicj.");
        return Mono.just(new TickTransactionsStatus(tick, 0, Map.of()));
    }

    @Override
    public Mono<TickInfo> getTickInfo() {
        return computorService.getCurrentTickInfo()
                .filter(ti -> ti.getTick() > 0)
                .map(mapper::map)
                .repeatWhenEmpty(RETRIES, repeat -> repeat.doOnNext(count -> log.info("Repeat [{}] getting current tick.", count)))
                .retryWhen(Retry.max(RETRIES).doBeforeRetry(rs -> log.info("Retry [{}] getting current tick.", rs.totalRetries())))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get current tick.")));
    }

    @Override
    public Mono<Long> getCurrentTick() {
        return getTickInfo().map(TickInfo::tick);
    }

    @Override
    public Mono<Long> getInitialTick() {
        return getTickInfo().map(TickInfo::initialTick);
    }

    @Override
    public Mono<TickData> getTickData(long tickNumber) {
        return computorService.getTickData((int) tickNumber)
                .repeatWhenEmpty(RETRIES, repeat -> repeat.doOnNext(count -> log.info("Repeat [{}] getting tick data [{}].", count, tickNumber)))
                .retryWhen(Retry.max(RETRIES).doBeforeRetry(rs -> log.info("Retry [{}] getting tick data [{}].", rs.totalRetries(), tickNumber)))
                .map(mapper::map);
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return computorService.getTickTransactions((int) tick)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .retryWhen(Retry.backoff(RETRIES, Duration.ofSeconds(1)))
                .doOnError(t -> log.error("Failed to get transactions for tick: [{}].", tick))
                .switchIfEmpty(computorService.getTickTransactions((int) tick).transformDeferred(BulkheadOperator.of(bulkhead))) // FIXME fix in qubicj
                .switchIfEmpty(computorService.getTickTransactions((int) tick).transformDeferred(BulkheadOperator.of(bulkhead))) // FIXME fix in qubicj
                .switchIfEmpty( // log a warning. That shouldn't happen often. // FIXME fix in qubicj
                        Mono.just(String.format("Received no transactions for tick [%d].", tick))
                                .doOnNext(log::warn)
                                .thenMany(Flux.empty())
                )
                .filter(this::isRelevantTransaction)
                .doOnNext(tx -> log.debug("Qx transaction: {}", tx))
                .map(mapper::map);
    }

    private boolean isRelevantTransaction(SignedTransaction stx) {
        at.qubic.api.domain.std.Transaction transaction = stx.getTransaction();
        if (isRelevantInputType(transaction) && isSentToQxAddress(transaction)) {
            log.debug("[{}]: [{}].", Qx.OrderType.fromCode(transaction.getInputType()), stx.getTransactionHash());
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
