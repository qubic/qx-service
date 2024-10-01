package org.qubic.qx.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.adapter.il.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
class IntegrationQxApiServiceIT extends AbstractIntegrationApiTest {

    private static final String TEST_ID = "TESTRAIJSNPOJAKARTQNQVRROKWBKLHXIBEYMYKVIGTWYXLDKFMEAFMDRJIC";
    private static final String CFB_ISSUER = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";

    private final WebClient webClient = createWebClient("http://localhost:1234");
    private final QxIntegrationMapper qxMapper = Mappers.getMapper(QxIntegrationMapper.class);
    private final IntegrationQxApiService apiClient = new IntegrationQxApiService(webClient, qxMapper);

    @Test
    void getFees() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        { "assetIssuanceFee": 1000000000, "transferFee": 1000000, "tradeFee": 5000000 }"""));

        StepVerifier.create(apiClient.getFees())
                .expectNext(new Fees(1_000_000_000, 1_000_000, 5_000_000))
                .verifyComplete();

        assertRequest("/v1/qx/getFees");
    }

    @Test
    void getAssetAskOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[ {"entityId":"%s","price":"3","numberOfShares":"100"} ]}""".formatted(TEST_ID)));

        StepVerifier.create(apiClient.getAssetAskOrders(CFB_ISSUER, "CFB")
                        .doOnNext(l -> log.info("{}", l)))
                .expectNext(List.of(new AssetOrder(TEST_ID, 3, 100)))
                .verifyComplete();

        assertRequest(String.format("/v1/qx/getAssetAskOrders?issuerId=%s&assetName=CFB&offset=0", CFB_ISSUER));
    }

    @Test
    void getAssetBidOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[ {"entityId":"%s","price":"3","numberOfShares":"100"} ]}""".formatted(TEST_ID)));

        StepVerifier.create(apiClient.getAssetBidOrders(CFB_ISSUER, "CFB")
                        .doOnNext(l -> log.info("{}", l)))
                .expectNext(List.of(new AssetOrder(TEST_ID, 3, 100)))
                .verifyComplete();

        assertRequest(String.format("/v1/qx/getAssetBidOrders?issuerId=%s&assetName=CFB&offset=0", CFB_ISSUER));
    }

    @Test
    void getEntityAskOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[
                           {"issuerId":"issuer","assetName":"asset","price":"42","numberOfShares": "666"}
                         ]}"""));

        StepVerifier.create(apiClient.getEntityAskOrders(TEST_ID)
                .doOnNext(l -> log.info("{}", l)))
                .expectNext(List.of(new EntityOrder("issuer", "asset", 42, 666)))
                .verifyComplete();

        assertRequest(String.format("/v1/qx/getEntityAskOrders?entityId=%s&offset=0", TEST_ID));
    }

    @Test
    void getEntityBidOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[
                           {"issuerId":"issuer","assetName":"asset","price":"42","numberOfShares": "666"}
                         ]}"""));

        StepVerifier.create(apiClient.getEntityBidOrders(TEST_ID)
                        .doOnNext(l -> log.info("{}", l)))
                .expectNext(List.of(new EntityOrder("issuer", "asset", 42, 666)))
                .verifyComplete();

        assertRequest(String.format("/v1/qx/getEntityBidOrders?entityId=%s&offset=0", TEST_ID));
    }

}