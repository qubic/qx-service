package org.qubic.qx.api.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.adapter.il.domain.IlAssetOrders;
import org.qubic.qx.api.adapter.il.domain.IlEntityOrders;
import org.qubic.qx.api.adapter.il.domain.IlFees;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class IntegrationQxApiService implements QxApiService {

    private static final int NUM_RETRIES = 1;
    private static final String QX_BASE_PATH_V1 = "/v1/qx";
    private final WebClient webClient;
    private final QxMapper qxMapper;

    public IntegrationQxApiService(WebClient webClient, QxMapper qxMapper) {
        this.webClient = webClient;
        this.qxMapper = qxMapper;
    }

    @Override
    public Fees getFees() {
        return webClient.get()
                .uri(QX_BASE_PATH_V1 + "/getFees")
                .retrieve()
                .bodyToMono(IlFees.class)
                .retry(NUM_RETRIES)
                .map(qxMapper::mapFees)
                .block();
    }

    @Override
    public List<AssetOrder> getAssetAskOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetAskOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .retry(NUM_RETRIES)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList)
                .block();
    }

    @Override
    public List<AssetOrder> getAssetBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .retry(NUM_RETRIES)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList)
                .block();
    }

    @Override
    public List<EntityOrder> getEntityAskOrders(String identity) {
        return webClient.get()
                .uri(entityOrderUri(QX_BASE_PATH_V1 + "/getEntityAskOrders", identity))
                .retrieve()
                .bodyToMono(IlEntityOrders.class)
                .retry(NUM_RETRIES)
                .map(IlEntityOrders::orders)
                .map(qxMapper::mapEntityOrderList)
                .block();
    }

    @Override
    public List<EntityOrder> getEntityBidOrders(String identity) {
        return webClient.get()
                .uri(entityOrderUri(QX_BASE_PATH_V1 + "/getEntityBidOrders", identity))
                .retrieve()
                .bodyToMono(IlEntityOrders.class)
                .retry(NUM_RETRIES)
                .map(IlEntityOrders::orders)
                .map(qxMapper::mapEntityOrderList)
                .block();
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
