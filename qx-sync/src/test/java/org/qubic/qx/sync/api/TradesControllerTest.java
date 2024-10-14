package org.qubic.qx.sync.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.api.service.TradesService;
import org.qubic.qx.sync.domain.Trade;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradesControllerTest {

    private final TradesService service = mock();
    private final TradesController controller = new TradesController(service);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/v1/qx")
            .build();

    private Trade trade1;
    private Trade trade2;
    private Trade trade3;

    @BeforeEach
    void setUp() {
        trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash1", true, "id1", "id2", "issuer1", "asset", 12, 34);
        trade2 = new Trade(43, Instant.EPOCH.getEpochSecond(), "hash2", true, "id2", "id1", "issuer2", "asset", 23, 45);
        trade3 = new Trade(43, Instant.EPOCH.getEpochSecond(), "hash3", true, "taker", "maker", "issuer2", "asset", 32, 56);
    }

    @Test
    void getTrades() {
        when(service.getTrades()).thenReturn(Flux.just(trade1, trade2, trade3));
        client.get().uri("/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Trade.class)
                .contains(trade1, trade2, trade3);
    }

    @Test
    void getAssetTrades() {
        when(service.getAssetTrades("issuer2", "asset")).thenReturn(Flux.just(trade2, trade3));
        client.get().uri("/issuer/issuer2/asset/asset/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Trade.class)
                .contains(trade2, trade3);
    }

    @Test
    void getEntityTrades() {
        when(service.getEntityTrades("id1")).thenReturn(Flux.just(trade1, trade2));
        client.get().uri("/entity/id1/trades")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Trade.class)
                .contains(trade1, trade2);
    }

}