package org.qubic.qx.sync.adapter.il;

import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.il.domain.IlAssetOrders;
import org.qubic.qx.sync.adapter.il.mapping.IlQxMapper;
import org.qubic.qx.sync.domain.AssetOrder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public class IntegrationQxApiService implements QxApiService {

    private static final int NUM_RETRIES = 1;
    private static final String QX_BASE_PATH_V1 = "/v1/qx";
    private final WebClient webClient;
    private final IlQxMapper qxMapper;

    public IntegrationQxApiService(WebClient webClient, IlQxMapper qxMapper) {
        this.webClient = webClient;
        this.qxMapper = qxMapper;
    }

    @Override public Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetAskOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .retry(NUM_RETRIES)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    @Override public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .retry(NUM_RETRIES)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    private static Function<UriBuilder, URI> assetOrderUri(String path, String issuer, String asset) {
        return uriBuilder -> uriBuilder
                .path(path)
                .queryParam("issuerId", issuer)
                .queryParam("assetName", asset)
                .queryParam("offset", 0)
                .build();
    }

}
