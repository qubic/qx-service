package org.qubic.qx.adapter.il;

import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.adapter.il.domain.IlEntityOrders;
import org.qubic.qx.adapter.il.domain.IlAssetOrders;
import org.qubic.qx.adapter.il.domain.IlFees;
import org.qubic.qx.adapter.il.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public class IntegrationQxApiService implements QxApiService {

    private static final String QX_BASE_PATH_V1 = "/v1/qx";
    private final WebClient webClient;
    private final QxIntegrationMapper qxMapper;

    public IntegrationQxApiService(WebClient webClient, QxIntegrationMapper qxMapper) {
        this.webClient = webClient;
        this.qxMapper = qxMapper;
    }

    @Override public Mono<Fees> getFees() {
        return webClient.get()
                .uri(QX_BASE_PATH_V1 + "/getFees")
                .retrieve()
                .bodyToMono(IlFees.class)
                .map(qxMapper::mapFees);
    }

    @Override public Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetAskOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    @Override public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList);
    }

    @Override public Mono<List<EntityOrder>> getEntityAskOrders(String identity) {
        return webClient.get()
                .uri(entityOrderUri(QX_BASE_PATH_V1 + "/getEntityAskOrders", identity))
                .retrieve()
                .bodyToMono(IlEntityOrders.class)
                .map(IlEntityOrders::orders)
                .map(qxMapper::mapEntityOrderList);
    }

    @Override public Mono<List<EntityOrder>> getEntityBidOrders(String identity) {
        return webClient.get()
                .uri(entityOrderUri(QX_BASE_PATH_V1 + "/getEntityBidOrders", identity))
                .retrieve()
                .bodyToMono(IlEntityOrders.class)
                .map(IlEntityOrders::orders)
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
