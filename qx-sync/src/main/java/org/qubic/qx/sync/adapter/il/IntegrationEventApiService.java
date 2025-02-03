package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.domain.EpochAndTick;
import org.qubic.qx.sync.domain.EventProcessingStatus;
import org.qubic.qx.sync.domain.TickEvents;
import org.qubic.qx.sync.domain.TransactionEvents;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.List;

@Slf4j
public class IntegrationEventApiService implements EventApiService {

    private final int retries;
    private static final String BASE_PATH = "/v1/events";
    private final WebClient webClient;

    public IntegrationEventApiService(WebClient webClient, int retries) {
        this.webClient = webClient;
        log.info("Number of retries: [{}]", retries);
        this.retries = retries;
    }

    @Override
    public Mono<List<TransactionEvents>> getTickEvents(long tick) {
        return webClient.post()
                .uri(BASE_PATH + "/getTickEvents")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(TickEvents.class)
                .map(TickEvents::txEvents)
                .switchIfEmpty(Mono.error(emptyGetEventsResult(tick)))
                .doOnError(e -> logError(String.format("Error getting tick events for tick [%d]", tick), e))
                .retryWhen(retrySpec());
    }

    @Override
    public Mono<EpochAndTick> getLastProcessedTick() {
        return webClient.get()
                .uri(BASE_PATH + "/status")
                .retrieve()
                .bodyToMono(EventProcessingStatus.class)
                .map(EventProcessingStatus::lastProcessedTick)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get event status.")))
                .doOnError(e -> logError("Error getting last processed tick", e))
                .retryWhen(retrySpec());
    }

    private RetryBackoffSpec retrySpec() {
        return Retry.backoff(retries, Duration.ofSeconds(1)).doBeforeRetry(c -> log.info("Retry: [{}].", c.totalRetries() + 1));
    }

    private static String tickPayloadBody(long tick) {
        return String.format("{\"tick\":%d}", tick);
    }

    private static EmptyResultException emptyGetEventsResult(long tick) {
        return new EmptyResultException(String.format("Could not get events for tick [%d].", tick));
    }

    private void logError(String logMessage, Throwable throwable) {
        ExceptionUtils.forEach(throwable,
                // here we warn only because we retry and log the error later, if retries are exhausted
                e -> log.warn("{}: {}", logMessage, ExceptionUtils.getMessage(e))
        );
    }


}
