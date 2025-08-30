package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.service.TradesService;
import org.qubic.qx.api.db.dto.TradeDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradesControllerTest {

    private final TradesService tradesService = mock();
    private final TradesController controller = new TradesController(tradesService);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .argumentResolvers(enablePaging())
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getTrades() {
        when(tradesService.getTrades(any(Pageable.class))).thenReturn(List.of(mock(), mock()));
        client.get().uri("/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getSmartContractTrades() {
        when(tradesService.getSmartContractTrades(any(Pageable.class))).thenReturn(List.of(mock(), mock()));
        client.get().uri("/smart-contract-trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getTokenTrades() {
        when(tradesService.getTokenTrades(any(Pageable.class))).thenReturn(List.of(mock(), mock()));
        client.get().uri("/token-trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getEntityTrades() {
        when(tradesService.getEntityTrades(eq("FOO"), any(Pageable.class))).thenReturn(List.of(mock(), mock()));
        client.get().uri("/entity/FOO/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    @Test
    void getAssetTrades() {
        when(tradesService.getAssetTrades(eq("ISSUER"), eq("ASSET"), any(Pageable.class))).thenReturn(List.of(mock(), mock()));
        client.get().uri("/issuer/ISSUER/asset/ASSET/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TradeDto.class)
                .hasSize(2);
    }

    private static Consumer<ArgumentResolverConfigurer> enablePaging() {
        return resolvers -> resolvers.addCustomResolver(new ReactivePageableHandlerMethodArgumentResolver());
    }

}