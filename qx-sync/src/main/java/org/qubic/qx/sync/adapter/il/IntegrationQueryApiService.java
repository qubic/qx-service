package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiLastProcessedTick;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTickData;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTickDataResponse;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTransaction;
import org.qubic.qx.sync.adapter.il.mapping.IlQueryApiMapper;
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
public class IntegrationQueryApiService implements CoreApiService {

    private static final String QUERY_API_BASE_PATH = "/query/v1";
    private final int retries;
    private final WebClient webClient;
    private final IlQueryApiMapper mapper;

    public IntegrationQueryApiService(WebClient webClient, IlQueryApiMapper mapper, int retries) {
        this.retries = retries;
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @Override
    public Mono<TickInfo> getTickInfo() {
        return webClient.get()
                .uri(QUERY_API_BASE_PATH + "/getLastProcessedTick")
                .retrieve()
                .bodyToMono(IlQueryApiLastProcessedTick.class)
                .map(mapper::map)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get tick info.")))
                .doOnError(e -> logError("Error getting tick info", e))
                .retryWhen(retrySpec());
    }

    @Override
    public Mono<Long> getCurrentTick() {
        return getTickInfo()
                .map(TickInfo::tick)
                .doOnNext(tick -> log.debug("Current tick: [{}]", tick))
                .doOnError(e -> logError("Error getting current tick", e));
    }

    @Override
    public Mono<TickData> getTickData(long tickNumber) {
        return webClient.post()
                .uri(QUERY_API_BASE_PATH + "/getTickData")
                .bodyValue(String.format("{\"tickNumber\":%d}", tickNumber))
                .retrieve()
                .bodyToMono(IlQueryApiTickDataResponse.class)
                .map(response -> response.tickData() == null
                        ? new IlQueryApiTickData(0,0,0) // empty tick
                        : response.tickData())
                .map(mapper::map)
                .switchIfEmpty(Mono.error(emptyResult("get tick data", tickNumber)))
                .doOnError(e -> logError(String.format("Error getting tick data for tick [%d]", tickNumber), e))
                .retryWhen(retrySpec());
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tickNumber) {
        return getTickTransactions(tickNumber)
                .filter(this::isRelevantTransaction)
                .mapNotNull(this::mapTransaction)
                .doOnError(e -> logError(String.format("Error getting qx transactions for tick [%d]", tickNumber), e));
    }

    private Transaction mapTransaction(IlQueryApiTransaction transaction) {
        try {
            return mapper.mapTransaction(transaction);
        } catch (Exception e) {
            log.error("Could not map transaction: {}", transaction, e);
            log.warn("Ignoring transaction [{}]", transaction.hash());
            return null;
        }
    }

    Flux<IlQueryApiTransaction> getTickTransactions(long tick) {
        return webClient.post()
                .uri(QUERY_API_BASE_PATH + "/getTransactionsForTick")
                .bodyValue(getTickTransactionsQuery(tick))
                .retrieve()
                .bodyToFlux(IlQueryApiTransaction.class) // can be empty (for example, empty transaction list for empty tick)
                .doOnError(e -> logError(String.format("Error getting tick transactions for tick [%d]", tick), e))
                .retryWhen(retrySpec());
    }

    private boolean isRelevantTransaction(IlQueryApiTransaction transaction) {
        if (isRelevantInputType(transaction)) {
            log.debug("[{}]: [{}].", Qx.OrderType.fromCode(transaction.inputType()), transaction.hash());
            return true;
        } else {
            return false;
        }
    }

    private static boolean isRelevantInputType(IlQueryApiTransaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains(transaction.inputType());
    }

    private static String getTickTransactionsQuery(long tickNumber) {
       return  """
                { "tickNumber": %d,
                  "filters": { "destination": "%s" },
                  "ranges": { "inputType": { "gt": "0" } }
                }""".formatted(tickNumber, Qx.QX_PUBLIC_ID);
    }

    private static EmptyResultException emptyResult(String action, long tick) {
        return new EmptyResultException(String.format("Could not %s for tick [%d].", action, tick));
    }

    private RetryBackoffSpec retrySpec() {
        return Retry.backoff(retries, Duration.ofSeconds(1)).doBeforeRetry(c -> log.info("Retry: [{}].", c.totalRetries() + 1));
    }

    private void logError(String logMessage, Throwable throwable) {
        ExceptionUtils.forEach(throwable,
                // here we warn only because we retry and log the error later if retries are exhausted
                e -> log.warn("{}: {}", logMessage, ExceptionUtils.getMessage(e))
        );
    }
}
