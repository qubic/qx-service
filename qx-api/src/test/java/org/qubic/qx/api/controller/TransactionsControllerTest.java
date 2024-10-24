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
    void getTransactions() {
        when(service.getTransactions()).thenReturn(List.of(mock(), mock()));
        client.get().uri("/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getTransactionsForAsset() {
        when(service.getTransactionsForAsset("ISSUER", "ASSET")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/issuer/ISSUER/asset/ASSET/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getTransactionsForEntity() {
        when(service.getTransactionsForEntity("IDENTITY")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/entity/IDENTITY/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getTransactionsForType() {
        when(service.getTransactionsForTypes(List.of(1,2,3))).thenReturn(List.of(mock(), mock()));
        client.get().uri("/transactions?inputTypes=1,2,3")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

}