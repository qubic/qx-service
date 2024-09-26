package org.qubic.qx.adapter.il.qx;

import org.qubic.qx.adapter.il.qx.domain.QxAssetOrders;
import org.qubic.qx.adapter.il.qx.domain.QxEntityOrders;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.qubic.qx.adapter.il.qx.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public class QxIntegrationApiService {

    private final WebClient webClient;
    private final QxIntegrationMapper qxMapper;

    public QxIntegrationApiService(WebClient webClient, QxIntegrationMapper qxMapper) {
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

    public Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri("/v1/qx/getAssetAskOrders", issuer, asset))
                .retrieve()
                .bodyToMono(QxAssetOrders.class)
                .map(QxAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri("/v1/qx/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(QxAssetOrders.class)
                .map(QxAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    public Mono<List<EntityOrder>> getEntityAskOrders(String identity) {
        return webClient.get()
                .uri(entityOrderUri("/v1/qx/getEntityAskOrders", identity))
                .retrieve()
                .bodyToMono(QxEntityOrders.class)
                .map(QxEntityOrders::orders)
                .map(qxMapper::mapEntityOrderList);
    }

    public Mono<List<EntityOrder>> getEntityBidOrders(String identity) {
        return webClient.get()
                .uri(entityOrderUri("/v1/qx/getEntityBidOrders", identity))
                .retrieve()
                .bodyToMono(QxEntityOrders.class)
                .map(QxEntityOrders::orders)
                .map(qxMapper::mapEntityOrderList);
    }

    private static Function<UriBuilder, URI> entityOrderUri(String path, String identity) {
        return uriBuilder -> uriBuilder
                .path(path)
                .queryParam("entityId", identity)
                .queryParam("offset", 0)
                .build();
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
