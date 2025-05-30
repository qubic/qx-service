package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.dto.TransactionDto;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

class TransactionsControllerSpringIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISPXHC";
    private static final String IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEOPXN";

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
    void getTransferTransactions() {
        client.get().uri("/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class);
    }

    @Test
    void getTransferTransactionsForAsset() {
        client.get().uri("/issuer/{:issuer}/asset/TEST/transfers", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class);
    }

    @Test
    void getTransferTransactionsForEntity() {
        client.get().uri("/entity/{:identity}/transfers", IDENTITY)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class);

    }

    @Test
    void getIssuedAssets() {
        client.get().uri("/issued-assets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class);
    }

}