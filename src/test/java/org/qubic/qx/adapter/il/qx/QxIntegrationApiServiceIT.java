package org.qubic.qx.adapter.il.qx;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.adapter.il.qx.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class QxIntegrationApiServiceIT {

    private static final String TEST_ID = "TESTRAIJSNPOJAKARTQNQVRROKWBKLHXIBEYMYKVIGTWYXLDKFMEAFMDRJIC";
    private static final String CFB_ISSUER = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";

    private final WebClient webClient = createWebClient("http://localhost:1234");
    private final QxIntegrationMapper qxMapper = Mappers.getMapper(QxIntegrationMapper.class);
    private final QxIntegrationApiService apiClient = new QxIntegrationApiService(webClient, qxMapper);

    private final MockWebServer integrationLayer = new MockWebServer();

    @Test
    void getFees() throws Exception {
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
    void getAssetAskOrders() throws Exception {
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
    void getAssetBidOrders() throws Exception {
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
    void getEntityAskOrders() throws Exception {
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
    void getEntityBidOrders() throws Exception {
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

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        integrationLayer.enqueue(response);
    }

    private void assertRequest(String expectedPath) throws InterruptedException {
        RecordedRequest request = integrationLayer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo(expectedPath);
    }

    @SuppressWarnings("SameParameterValue")
    private @NotNull WebClient createWebClient(String baseUrl) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(1));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .build();
    }

    @BeforeEach
    void setUp() throws Exception {
        integrationLayer.start(1234);
    }

    @AfterEach
    void tearDown() throws Exception {
        integrationLayer.shutdown();
    }

}