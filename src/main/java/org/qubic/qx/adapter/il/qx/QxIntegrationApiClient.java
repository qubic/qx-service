package org.qubic.qx.adapter.il.qx;

import org.qubic.qx.adapter.il.qx.domain.QxAssetOrders;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.qubic.qx.adapter.il.qx.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public class QxIntegrationApiClient {

    private final WebClient webClient;
    private final QxIntegrationMapper qxMapper;

    public QxIntegrationApiClient(WebClient webClient, QxIntegrationMapper qxMapper) {
        this.webClient = webClient;
        this.qxMapper = qxMapper;
    }

    public Mono<Fees> getFees() {
        return webClient.get()
                .uri("/v1/qx/getFees")
                .retrieve()
                .bodyToMono(QxFees.class)
                .map(qxMapper::mapFees);
    }

    public Mono<List<AssetOrder>> getAskOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri("/v1/qx/getAssetAskOrders", issuer, asset))
                .retrieve()
                .bodyToMono(QxAssetOrders.class)
                .map(QxAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    public Mono<List<AssetOrder>> getBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri("/v1/qx/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(QxAssetOrders.class)
                .map(QxAssetOrders::orders)
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
