package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.adapter.il.domain.IlTickData;
import org.qubic.qx.sync.adapter.il.domain.IlTickInfo;
import org.qubic.qx.sync.adapter.il.domain.IlTransaction;
import org.qubic.qx.sync.adapter.il.domain.IlTransactions;
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
    private final int retries;
    private final WebClient webClient;
    private final IlCoreMapper mapper;

    public IntegrationCoreApiService(WebClient webClient, IlCoreMapper mapper, int retries) {
        this.webClient = webClient;
        this.mapper = mapper;
        log.info("Number of retries: [{}]", retries);
        this.retries = retries;
    }

    @Override
    public Mono<Long> getCurrentTick() {
        return getTickInfo()
                .map(TickInfo::tick)
                .doOnNext(tick -> log.debug("Current tick: [{}]", tick))
                .doOnError(e -> logError("Error getting current tick", e));
    }

    @Override
    public Mono<TickInfo> getTickInfo() {
        return webClient.get()
                .uri(CORE_BASE_PATH_V1 + "/getTickInfo")
                .retrieve()
                .bodyToMono(IlTickInfo.class)
                .map(mapper::map)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get tick info.")))
                .doOnError(e -> logError("Error getting tick info", e))
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
                .doOnError(e -> logError(String.format("Error getting tick data for tick [%d]", tick), e))
                .retryWhen(retrySpec());
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return getTickTransactions(tick)
                .flatMapMany(txs -> Flux.fromIterable(txs.transactions()))
                .filter(this::isRelevantTransaction)
                .mapNotNull(this::mapTransaction)
                .doOnError(e -> logError(String.format("Error getting qx transactions for tick [%d]", tick), e));
    }

    private Transaction mapTransaction(IlTransaction transaction) {
        try {
            return mapper.mapTransaction(transaction);
        } catch (Exception e) {
            log.error("Could not map transaction: {}", transaction, e);
            log.warn("Ignoring transaction [{}]", transaction.txId());
            return null;
        }
    }

    Mono<IlTransactions> getTickTransactions(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickTransactions")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(IlTransactions.class)
                .switchIfEmpty(Mono.error(emptyResult("get tick transactions", tick)))
                .doOnError(e -> logError(String.format("Error getting tick transactions for tick [%d]", tick), e))
                .retryWhen(retrySpec());
    }

    private RetryBackoffSpec retrySpec() {
        return Retry.backoff(retries, Duration.ofSeconds(1)).doBeforeRetry(c -> log.info("Retry: [{}].", c.totalRetries() + 1));
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
        return Strings.CS.equals(transaction.destId(), Qx.QX_PUBLIC_ID);
    }

    private static boolean isRelevantInputType(IlTransaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains(transaction.inputType());
    }

    private static EmptyResultException emptyResult(String action, long tick) {
        return new EmptyResultException(String.format("Could not %s for tick [%d].", action, tick));
    }

    private void logError(String logMessage, Throwable throwable) {
        ExceptionUtils.forEach(throwable,
                // here we warn only because we retry and log the error later if retries are exhausted
                e -> log.warn("{}: {}", logMessage, ExceptionUtils.getMessage(e))
        );
    }

}
