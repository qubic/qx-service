package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.adapter.il.domain.*;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QxFunctionsControllerSpringIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH";
    private static final String ID = ISSUER;

    private WebTestClient client;

    @BeforeEach
    public void setUpTestNode(WebApplicationContext context) {
        client = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/service/v1/qx")
                .build();
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

        client.get().uri("/issuer/"+ISSUER+"/asset/TEST/asks")
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

        client.get().uri("/issuer/"+ISSUER+"/asset/TEST/bids")
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

        client.get().uri("/entity/"+ ID + "/asks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .isEqualTo(List.of(new EntityOrder("issuer", "asset", 1, 2)));

        assertThat(integrationLayer.takeRequest().getPath()).contains("/v1/qx/getEntityAskOrders?entityId=" + ID);
    }

    @Test
    void getEntityBidOrders() throws Exception {
        IlEntityOrder entityOrder = new IlEntityOrder("issuer", "asset", "1", "2");
        IlEntityOrders entityOrders = new IlEntityOrders(List.of(entityOrder));
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(JsonUtil.toJson(entityOrders)));

        client.get().uri("/entity/" + ID + "/bids")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .isEqualTo(List.of(new EntityOrder("issuer", "asset", 1, 2)));

        assertThat(integrationLayer.takeRequest().getPath()).contains("/v1/qx/getEntityBidOrders?entityId=" + ID);
    }

}