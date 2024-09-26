package org.qubic.qx.adapter.il.qx;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.adapter.il.qx.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.Fees;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class QxIntegrationApiClientIT {

//     private static final String BASE_URL = "http://95.216.243.140/";
    private static final String BASE_URL = "http://localhost:1234/";

    private final WebClient webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
            .build();
    private final QxIntegrationMapper qxMapper = Mappers.getMapper(QxIntegrationMapper.class);
    private final QxIntegrationApiClient apiClient = new QxIntegrationApiClient(webClient, qxMapper);

    private final MockWebServer integrationLayer = new MockWebServer();

    @BeforeEach
    void setUp() throws Exception {
        integrationLayer.start(1234);
    }

    @AfterEach
    void tearDown() throws Exception {
        integrationLayer.shutdown();
    }

    @Test
    void getFees() throws Exception {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        { "assetIssuanceFee": 1000000000, "transferFee": 1000000, "tradeFee": 5000000 }"""));

        StepVerifier.create(apiClient.getFees().timeout(Duration.ofSeconds(1)))
                .expectNext(new Fees(1_000_000_000, 1_000_000, 5_000_000))
                .verifyComplete();

        assertRequest("/v1/qx/getFees");
    }

    @Test
    void getAskOrders() throws Exception {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[{"entityId":"BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC",
                         "price": "3","numberOfShares": "100"}]}"""));

        StepVerifier.create(apiClient.getAskOrders("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "CFB")
                        .timeout(Duration.ofSeconds(1))
                        .doOnNext(l -> log.info("{}", l)))
                .assertNext(l -> assertThat(l).isNotEmpty())
                .verifyComplete();

        assertRequest("/v1/qx/getAssetAskOrders?issuerId=CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL&assetName=CFB&offset=0");
    }

    private void assertRequest(String expectedPath) throws InterruptedException {
        RecordedRequest request = integrationLayer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo(expectedPath);
    }

    @Test
    void getBidOrders() throws Exception {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[{"entityId":"BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC",
                         "price": "3","numberOfShares": "100"}]}"""));

        StepVerifier.create(apiClient.getBidOrders("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "CFB")
                        .timeout(Duration.ofSeconds(1))
                        .doOnNext(l -> log.info("{}", l)))
                .assertNext(l -> assertThat(l).isNotEmpty())
                .verifyComplete();

        assertRequest("/v1/qx/getAssetBidOrders?issuerId=CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL&assetName=CFB&offset=0");
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        integrationLayer.enqueue(response);
    }

}