package org.qubic.qx.api.adapter.il;

import org.qubic.qx.api.adapter.il.domain.IlTickInfo;
import org.qubic.qx.api.adapter.il.domain.IlTickInfoRespoonse;
import org.qubic.qx.api.adapter.il.domain.QuerySmartContractRequest;
import org.qubic.qx.api.adapter.il.domain.QuerySmartContractResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;

public class IntegrationLiveClient {

    private static final int NUM_RETRIES = 2;
    private static final String LIVE_BASE_PATH_V1 = "/live/v1";
    private final WebClient webClient;

    public IntegrationLiveClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public BigInteger getLatestTick() {
        return webClient.get()
                .uri(LIVE_BASE_PATH_V1 + "/tick-info")
                .retrieve()
                .bodyToMono(IlTickInfoRespoonse.class)
                .map(IlTickInfoRespoonse::tickInfo)
                .map(IlTickInfo::tick)
                .retry(NUM_RETRIES)
                .block();
    }

    public QuerySmartContractResponse querySmartContract(QuerySmartContractRequest request) {
        return webClient.post()
                .uri(LIVE_BASE_PATH_V1 + "/querySmartContract")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QuerySmartContractResponse.class)
                .retry(NUM_RETRIES)
                .block();
    }

}
