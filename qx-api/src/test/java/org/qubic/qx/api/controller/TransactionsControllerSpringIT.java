package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

class TransactionsControllerSpringIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH";
    private static final String IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHA";

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
    void getOrderTransactions() {
        client.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class);
    }

    @Test
    void getOrderTransactionsForAsset() {
        client.get().uri("/issuer/{:issuer}/asset/TEST/orders", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class);
    }

    @Test
    void getOrderTransactionsForEntity() {
        client.get().uri("/entity/{:identity}/orders", IDENTITY)
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