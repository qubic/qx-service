package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.EventApiService;
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
                .retry(NUM_RETRIES)
                .doOnError(e -> log.error("Error getting tick events.", e))
                .map(TickEvents::txEvents);
    }

    @Override
    public Mono<EpochAndTick> getLastProcessedTick() {
        return webClient.get()
                .uri(BASE_PATH + "/status")
                .retrieve()
                .bodyToMono(EventProcessingStatus.class)
                .retry(NUM_RETRIES)
                .map(EventProcessingStatus::lastProcessedTick)
                .doOnError(e -> log.error("Error getting tick events.", e));
    }

    private static String tickPayloadBody(long tick) {
        return String.format("{\"tick\":%d}", tick);
    }

}
