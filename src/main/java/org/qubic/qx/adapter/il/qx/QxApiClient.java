package org.qubic.qx.adapter.il.qx;

import org.qubic.qx.adapter.il.qx.domain.QxAssetOrder;
import org.qubic.qx.adapter.il.qx.domain.QxAssetOrders;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public class QxApiClient {

    private final WebClient webClient;

    public QxApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<QxFees> getFees() {
        return webClient.get()
                .uri("/v1/qx/getFees")
                .retrieve()
                .bodyToMono(QxFees.class);
    }

    public Mono<List<QxAssetOrder>> getAskOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri("/v1/qx/getAssetAskOrders", issuer, asset))
                .retrieve()
                .bodyToMono(QxAssetOrders.class)
                .map(QxAssetOrders::orders);
    }

    public Mono<List<QxAssetOrder>> getBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri("/v1/qx/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(QxAssetOrders.class)
                .map(QxAssetOrders::orders);
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
