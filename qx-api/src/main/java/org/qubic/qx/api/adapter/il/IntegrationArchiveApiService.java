package org.qubic.qx.api.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.adapter.CoreArchiveApiService;
import org.qubic.qx.api.adapter.domain.TickData;
import org.qubic.qx.api.adapter.il.domain.IlTickDataResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class IntegrationArchiveApiService implements CoreArchiveApiService {

    private static final int NUM_RETRIES = 1;
    private final WebClient webClient;
    private final ArchiveMapper archiveMapper;

    public IntegrationArchiveApiService(WebClient webClient, ArchiveMapper archiveMapper) {
        this.webClient = webClient;
        this.archiveMapper = archiveMapper;
    }

    @Override
    public TickData getTickData(long tick) {
        return webClient.get()
                .uri("/v1/ticks/{tick}/tick-data", tick)
                .retrieve()
                .bodyToMono(IlTickDataResponse.class)
                .retry(NUM_RETRIES)
                .map(IlTickDataResponse::tickData)
                .map(archiveMapper::map)
                .doOnError(t -> log.error(t.toString(), t))
                .block();
    }

}
