package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.*;

class TransactionsControllerTest {

    private final TransactionsService service = mock();
    private final TransactionsController controller = new TransactionsController(service);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getTransferTransactions() {
        when(service.getTransferTransactions()).thenReturn(List.of(mock(), mock()));
        client.get().uri("/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getTransferTransactionsForAsset() {
        when(service.getTransferTransactionsForAsset("ISSUER", "ASSET")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/issuer/ISSUER/asset/ASSET/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getTransferTransactionsForEntity() {
        when(service.getTransferTransactionsForEntity("IDENTITY")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/entity/IDENTITY/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getOrderTransactions() {
        when(service.getOrderTransactions()).thenReturn(List.of(mock(), mock()));
        client.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getOrderTransactionsForAsset() {
        when(service.getOrderTransactionsForAsset("ISSUER", "ASSET")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/issuer/ISSUER/asset/ASSET/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getOrderTransactionsForEntity() {
        when(service.getOrderTransactionsForEntity("IDENTITY")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/entity/IDENTITY/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

}