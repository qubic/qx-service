package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.adapter.il.domain.IlAssetOrders;
import org.qubic.qx.sync.adapter.il.mapping.IlQxMapper;
import org.qubic.qx.sync.domain.AssetOrder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class IntegrationQxApiService implements QxApiService {

    private static final int NUM_RETRIES = 3;
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
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList)
                .switchIfEmpty(Mono.error(emptyResult("asks", issuer, asset)))
                .doOnError(e -> log.error("Error getting ask orders: {}", e.getMessage()))
                .retryWhen(retrySpec());
    }

    @Override public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return webClient.get()
                .uri(assetOrderUri(QX_BASE_PATH_V1 + "/getAssetBidOrders", issuer, asset))
                .retrieve()
                .bodyToMono(IlAssetOrders.class)
                .map(IlAssetOrders::orders)
                .map(qxMapper::mapAssetOrderList)
                .switchIfEmpty(Mono.error(emptyResult("bids", issuer, asset)))
                .doOnError(e -> log.error("Error getting bid orders: {}", e.getMessage()))
                .retryWhen(retrySpec());
    }

    private static RetryBackoffSpec retrySpec() {
        return Retry.backoff(NUM_RETRIES, Duration.ofSeconds(1)).doBeforeRetry(c -> log.info("Retry: [{}].", c.totalRetries() + 1));
    }

    private static EmptyResultException emptyResult(String action, String issuer, String asset) {
        return new EmptyResultException(String.format("Empty response getting %s for asset [%s/%s].", action, issuer, asset));
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
