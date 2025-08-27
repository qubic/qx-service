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
    void getAssetTrades() {
        client.get().uri("/issuer/{:issuer}/asset/ASSET/trades", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(0);
    }

}
