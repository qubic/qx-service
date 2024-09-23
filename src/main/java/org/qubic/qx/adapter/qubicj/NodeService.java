package org.qubic.qx.adapter.qubicj;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import lombok.extern.slf4j.Slf4j;
import at.qubic.api.domain.std.SignedTransaction;
import at.qubic.api.service.ComputorService;
import org.qubic.qx.adapter.exception.EmptyResultException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
public class NodeService {

    private final ComputorService computorService;

    private final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(5)
            .build();
    private final Bulkhead bulkhead = Bulkhead.of("getTickTransactionsBulkhead", bulkheadConfig);

    public NodeService(ComputorService computorService) {
        this.computorService = computorService;
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
                .doOnNext(tickNumber -> log.info("Initial tick number: {}", tickNumber))
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get initial tick.")));
    }

    public Flux<SignedTransaction> getTickTransactions(long tick) {
        return computorService.getTickTransactions((int) tick)
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                .doOnError(t -> log.error("Failed to get transactions for tick: [{}].", tick))
                ;
    }

}
