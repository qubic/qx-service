package org.qubic.qx.sync.api;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.il.domain.*;
import org.qubic.qx.sync.api.domain.AssetOrder;
import org.qubic.qx.sync.api.domain.EntityOrder;
import org.qubic.qx.sync.api.domain.Fees;
import org.qubic.qx.sync.util.JsonUtil;
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
    il.client.scheme=http
    il.client.host=localhost
    il.client.port=1234
    backend=integration
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
    void getFees() {
        IlFees ilFees = new IlFees(1L, 2L, 3L);
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(ilFees)));

        client.get().uri("/fees")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fees.class)
                .isEqualTo(new Fees(1L, 2L, 3L));
    }

    @Test
    void getAssetAskOrders() throws Exception {
        IlAssetOrder assetOrder = new IlAssetOrder("entity", "1", "2");
        IlAssetOrders assetOrders = new IlAssetOrders(List.of(assetOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(assetOrders)));

        client.get().uri("/issuer/issuerId/asset/assetName/orders/ask")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(List.of(new AssetOrder("entity", 1L, 2L)));

        assertThat(integrationLayer.takeRequest().getPath()).contains("/v1/qx/getAssetAskOrders");
    }

    @Test
    void getAssetBidOrders() throws Exception {
        IlAssetOrder assetOrder = new IlAssetOrder("entity", "1", "2");
        IlAssetOrders assetOrders = new IlAssetOrders(List.of(assetOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(assetOrders)));

        client.get().uri("/issuer/issuerId/asset/assetName/orders/bid")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(List.of(new AssetOrder("entity", 1L, 2L)));

        assertThat(integrationLayer.takeRequest().getPath()).contains("/v1/qx/getAssetBidOrders");
    }

    @Test
    void getEntityAskOrders() throws Exception {
        IlEntityOrder entityOrder = new IlEntityOrder("issuer", "asset", "1", "2");
        IlEntityOrders entityOrders = new IlEntityOrders(List.of(entityOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(entityOrders)));

        client.get().uri("/entity/identity/orders/ask")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .isEqualTo(List.of(new EntityOrder("issuer", "asset", 1, 2)));

        assertThat(integrationLayer.takeRequest().getPath()).contains("/v1/qx/getEntityAskOrders?entityId=identity");
    }

    @Test
    void getEntityBidOrders() throws Exception {
        IlEntityOrder entityOrder = new IlEntityOrder("issuer", "asset", "1", "2");
        IlEntityOrders entityOrders = new IlEntityOrders(List.of(entityOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(entityOrders)));

        client.get().uri("/entity/identity/orders/bid")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .isEqualTo(List.of(new EntityOrder("issuer", "asset", 1, 2)));

        assertThat(integrationLayer.takeRequest().getPath()).contains("/v1/qx/getEntityBidOrders?entityId=identity");
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        integrationLayer.enqueue(response);
    }

}