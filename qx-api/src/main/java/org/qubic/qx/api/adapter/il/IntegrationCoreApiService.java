package org.qubic.qx.api.adapter.il;

import org.qubic.qx.api.adapter.CoreApiService;
import org.qubic.qx.api.adapter.il.domain.IlTickInfo;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;

public class IntegrationCoreApiService implements CoreApiService {

    private static final int NUM_RETRIES = 1;
    private static final String CORE_BASE_PATH_V1 = "/v1/core";
    private final WebClient webClient;

    public IntegrationCoreApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public BigInteger getLatestTick() {
        return webClient.get()
                .uri(CORE_BASE_PATH_V1 + "/getTickInfo")
                .retrieve()
                .bodyToMono(IlTickInfo.class)
                .map(IlTickInfo::tick)
                .retry(NUM_RETRIES)
                .block();
    }
}
