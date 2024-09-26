package org.qubic.qx.api;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.il.qx.domain.QxAssetOrder;
import org.qubic.qx.adapter.il.qx.domain.QxAssetOrders;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.Fees;
import org.qubic.qx.util.JsonUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = """
il.base-url=http://localhost:1234
""")
class QxFunctionsControllerSpringIT {

    private final MockWebServer integrationLayer = new MockWebServer();
    private WebTestClient client;

    @BeforeEach
    public void setUpTestNode(ApplicationContext context) throws IOException {
        client = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/v1/qx")
                .build();
        integrationLayer.start(1234);
    }
    @AfterEach
    public void shutdown() throws Exception {
        this.integrationLayer.shutdown();
    }

    @Test
    void getFees() throws Exception {
        QxFees qxFees = new QxFees(1L, 2L, 3L);
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(qxFees)));

        client.get().uri("/fees")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fees.class)
                .isEqualTo(new Fees(1L, 2L, 3L));

        RecordedRequest request = integrationLayer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/qx/getFees");
    }

    @Test
    void getAssetAskOrders() throws Exception {
        QxAssetOrder assetOrder = new QxAssetOrder("entity", "1", "2");
        QxAssetOrders assetOrders = new QxAssetOrders(List.of(assetOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(assetOrders)));

        client.get().uri("/issuer/issuerId/asset/assetName/orders/ask")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(List.of(new AssetOrder("entity", 1L, 2L)));

        RecordedRequest request = integrationLayer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/qx/getAssetAskOrders?issuerId=issuerId&assetName=assetName&offset=0");
    }

    @Test
    void getAssetBidOrders() throws Exception {
        QxAssetOrder assetOrder = new QxAssetOrder("entity", "1", "2");
        QxAssetOrders assetOrders = new QxAssetOrders(List.of(assetOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(assetOrders)));

        client.get().uri("/issuer/issuerId/asset/assetName/orders/bid")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(List.of(new AssetOrder("entity", 1L, 2L)));

        RecordedRequest request = integrationLayer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/qx/getAssetBidOrders?issuerId=issuerId&assetName=assetName&offset=0");
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        integrationLayer.enqueue(response);
    }

}