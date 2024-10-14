package org.qubic.qx.sync.api.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.Trade;
import org.qubic.qx.sync.repository.TradeRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradesServiceTest {

    private final TradeRepository tradeRepository = mock();
    private final TradesService service = new TradesService(tradeRepository);

    @Test
    void getTrades() {
        Trade trade1 = mock();
        Trade trade2 = mock();
        when(tradeRepository.findTrades(any(Instant.class), any(Instant.class))).thenReturn(Flux.just(trade1, trade2));

        StepVerifier.create(service.getTrades())
                .expectNext(trade1, trade2)
                .verifyComplete();
    }

    @Test
    void getAssetTrades() {
        Trade trade1 = mock();
        Trade trade2 = mock();
        when(trade2.issuer()).thenReturn("foo");
        when(trade2.assetName()).thenReturn("bar");
        when(tradeRepository.findTrades(any(Instant.class), any(Instant.class))).thenReturn(Flux.just(trade1, trade2));

        StepVerifier.create(service.getAssetTrades("foo", "bar"))
                .expectNext(trade2)
                .verifyComplete();
    }

    @Test
    void getEntityTrades() {
        Trade trade1 = mock();
        Trade trade2 = mock();
        Trade trade3 = mock();
        when(trade2.maker()).thenReturn("foo");
        when(trade3.taker()).thenReturn("foo");
        when(tradeRepository.findTrades(any(Instant.class), any(Instant.class))).thenReturn(Flux.just(trade1, trade2, trade3));

        StepVerifier.create(service.getEntityTrades("foo"))
                .expectNext(trade2, trade3)
                .verifyComplete();
    }

}