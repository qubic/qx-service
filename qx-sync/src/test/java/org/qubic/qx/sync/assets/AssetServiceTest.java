package org.qubic.qx.sync.assets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.domain.OrderBook;
import org.qubic.qx.sync.repository.OrderBookRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class AssetServiceTest {

    private final Assets assets = new Assets();
    private final QxApiService qxApiService = mock();
    private final OrderBookRepository orderBookRepository = mock();
    private final AssetService assetService = new AssetService(assets, qxApiService, orderBookRepository);

    @BeforeEach
    void initAssets() {
        assets.add(new Asset("issuer1", "asset1"));
        assets.add(new Asset("issuer2", "asset2"));
    }

    @Test
    void initializeOrderBooks() {
        OrderBook ob1 = new OrderBook(42, "issuer1", "asset1", List.of(), List.of());

        when(orderBookRepository.hasOrderBook("issuer1", "asset1")).thenReturn(Mono.just(false));
        when(orderBookRepository.hasOrderBook("issuer2", "asset2")).thenReturn(Mono.just(true));

        when(qxApiService.getAssetAskOrders("issuer1", "asset1")).thenReturn(Mono.just(List.of()));
        when(qxApiService.getAssetBidOrders("issuer1", "asset1")).thenReturn(Mono.just(List.of()));
        when(orderBookRepository.storeOrderBook(ob1)).thenReturn(Mono.just(1L));

        StepVerifier.create(assetService.initializeOrderBooks(42L))
                .expectNext(ob1)
                .verifyComplete();
    }

    @Test
    void retrieveCurrentOrderBooks() {
        OrderBook ob1 = new OrderBook(42, "issuer1", "asset1", List.of(), List.of());
        OrderBook ob2 = new OrderBook(42, "issuer2", "asset2", List.of(), List.of());

        when(qxApiService.getAssetAskOrders("issuer1", "asset1")).thenReturn(Mono.just(List.of()));
        when(qxApiService.getAssetBidOrders("issuer1", "asset1")).thenReturn(Mono.just(List.of()));
        when(orderBookRepository.storeOrderBook(ob1)).thenReturn(Mono.just(1L));

        when(orderBookRepository.getOrderBook("issuer1", "asset1", 42)).thenReturn(Mono.empty());
        when(orderBookRepository.getOrderBook("issuer2", "asset2", 42)).thenReturn(Mono.just(ob2));

        StepVerifier.create(assetService.retrieveCurrentOrderBooks(42L, assets.getAssets()))
                .expectNext(ob1)
                .expectNext(ob2)
                .verifyComplete();
    }

    @Test
    void loadLatestOrderBooksBeforeTick() {
        OrderBook ob1 = new OrderBook(41, "issuer1", "asset1", List.of(), List.of());

        when(orderBookRepository.getPreviousOrderBookBefore("issuer1", "asset1", 42)).thenReturn(Mono.just(ob1));
        when(orderBookRepository.getPreviousOrderBookBefore("issuer2", "asset2", 42)).thenReturn(Mono.empty());

        StepVerifier.create(assetService.loadLatestOrderBooksBeforeTick(42L, assets.getAssets()))
                .expectNext(ob1)
                .verifyComplete();
    }
}