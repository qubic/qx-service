package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.adapter.il.domain.*;
import org.qubic.qx.sync.adapter.il.mapping.IlCoreMapper;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Slf4j
public class IntegrationCoreApiService implements CoreApiService {

    private static final String CORE_BASE_PATH_V1 = "/v1/core";
    private static final int NUM_RETRIES = 3;
    private final WebClient webClient;
    private final IlCoreMapper mapper;

    public IntegrationCoreApiService(WebClient webClient, IlCoreMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @Override
    public Mono<Long> getCurrentTick() {
        return getTickInfo()
                .map(TickInfo::tick)
                .doOnNext(tick -> log.debug("Current tick: [{}]", tick))
                .doOnError(e -> log.error("Error getting current tick: {}", e.getMessage()));
    }

    @Override
    public Mono<TickInfo> getTickInfo() {
        return webClient.get()
                .uri(CORE_BASE_PATH_V1 + "/getTickInfo")
                .retrieve()
                .bodyToMono(IlTickInfo.class)
                .map(mapper::map)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get tick info.")))
                .doOnError(e -> log.error("Error getting tick info: {}", e.getMessage()))
                .retryWhen(retrySpec());
    }

    @Override
    public Mono<TickData> getTickData(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickData")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(IlTickData.class)
                .map(mapper::map)
                .switchIfEmpty(Mono.error(emptyResult("get tick data", tick)))
                .doOnError(e -> log.error("Error getting tick data: {}", e.getMessage()))
                .retryWhen(retrySpec());
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return getTickTransactions(tick)
                .flatMapMany(txs -> Flux.fromIterable(txs.transactions()))
                .filter(this::isRelevantTransaction)
                .map(tx -> mapper.mapTransaction(tx, null))
                .doOnError(e -> log.error("Error getting qx transactions: {}", e.getMessage()));
    }

    Mono<IlTransactions> getTickTransactions(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickTransactions")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(IlTransactions.class)
                .switchIfEmpty(Mono.error(emptyResult("get tick transactions", tick)))
                .doOnError(e -> log.error("Error getting tick transactions: {}", e.getMessage()))
                .retryWhen(retrySpec());
    }

    private static RetryBackoffSpec retrySpec() {
        return Retry.backoff(NUM_RETRIES, Duration.ofSeconds(1)).doBeforeRetry(c -> log.info("Retry: [{}].", c.totalRetries() + 1));
    }

    private boolean isRelevantTransaction(IlTransaction transaction) {
        if (isRelevantInputType(transaction) && isSentToQxAddress(transaction)) {
            log.debug("[{}]: [{}].", Qx.OrderType.fromCode(transaction.inputType()), transaction.txId());
            return true;
        } else {
            return false;
        }
    }

    private static String tickPayloadBody(long tick) {
        return String.format("{\"tick\":%d}", tick);
    }

    private static boolean isSentToQxAddress(IlTransaction transaction) {
        return StringUtils.equals(transaction.destId(), Qx.QX_PUBLIC_ID);
    }

    private static boolean isRelevantInputType(IlTransaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains(transaction.inputType());
    }

    private static EmptyResultException emptyResult(String action, long tick) {
        return new EmptyResultException(String.format("Could not %s for tick [%d].", action, tick));
    }

}
