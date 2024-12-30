package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.controller.service.TradesService;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradesControllerTest {

    private final TradesService tradesService = mock();
    private final TradesController controller = new TradesController(tradesService);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getTrades() {
        when(tradesService.getTrades()).thenReturn(List.of(mock(), mock()));
        client.get().uri("/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getEntityTrades() {
        when(tradesService.getEntityTrades("FOO")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/entity/FOO/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getAssetTrades() {
        when(tradesService.getAssetTrades("ISSUER", "ASSET")).thenReturn(List.of(mock(), mock()));
        client.get().uri("/issuer/ISSUER/asset/ASSET/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

}