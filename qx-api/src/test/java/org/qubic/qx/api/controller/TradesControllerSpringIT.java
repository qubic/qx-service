package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.dto.TradeDto;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

public class TradesControllerSpringIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISPXHC";
    private static final String ENTITY = "ENTITYENTITYENTITYENTITYENTITYENTITYENTITYENTITYENTITYENJLPE";

    private WebTestClient client;

    @BeforeEach
    public void setUpClient(WebApplicationContext context) {
        client = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/service/v1/qx")
                .build();
    }

    @Test
    void getTrades() {
        client.get().uri("/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(0);
    }

    @Test
    void getSmartContractTrades() {
        client.get().uri("/smart-contract-trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(0);
    }

    @Test
    void getTokenTrades() {
        client.get().uri("/token-trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(0);
    }

    @Test
    void getAssetTrades() {
        client.get().uri("/issuer/{:issuer}/asset/ASSET/trades", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(0);
    }

    @Test
    void getEntityTrades() {
        client.get().uri("/entity/{:entity}/trades", ENTITY)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(0);
    }

}
