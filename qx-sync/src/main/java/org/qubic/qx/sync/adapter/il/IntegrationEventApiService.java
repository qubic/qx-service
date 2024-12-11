package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.domain.EpochAndTick;
import org.qubic.qx.sync.domain.EventProcessingStatus;
import org.qubic.qx.sync.domain.TickEvents;
import org.qubic.qx.sync.domain.TransactionEvents;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class IntegrationEventApiService implements EventApiService {

    private static final int NUM_RETRIES = 1;
    private static final String BASE_PATH = "/v1/events";
    private final WebClient webClient;

    public IntegrationEventApiService(WebClient webClient) {
        this.webClient = webClient;
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
                .doOnError(e -> log.error("Error getting tick events: {}", e.getMessage()))
                .retry(NUM_RETRIES);
    }

    @Override
    public Mono<EpochAndTick> getLastProcessedTick() {
        return webClient.get()
                .uri(BASE_PATH + "/status")
                .retrieve()
                .bodyToMono(EventProcessingStatus.class)
                .map(EventProcessingStatus::lastProcessedTick)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get event status.")))
                .doOnError(e -> log.error("Error getting last processed tick: {}", e.getMessage()))
                .retry(NUM_RETRIES);
    }

    private static String tickPayloadBody(long tick) {
        return String.format("{\"tick\":%d}", tick);
    }

    private static EmptyResultException emptyGetEventsResult(long tick) {
        return new EmptyResultException(String.format("Could not get events for tick [%d].", tick));
    }

}
