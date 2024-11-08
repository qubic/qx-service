package org.qubic.qx.sync.job;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.Qx.OrderType;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.assets.AssetService;
import org.qubic.qx.sync.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderBookProcessorTest {

    private final CoreApiService coreApiService = mock();
    private final AssetService assetService = mock();
    private final OrderBookCalculator orderBookCalculator = mock();
    private final OrderBookProcessor orderBookProcessor = new OrderBookProcessor(coreApiService, assetService, orderBookCalculator);

    @Test
    void processQxTransactions_thenStoreTradeInformation() {

        OrderType orderType = OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "assetName", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);
        Asset asset = new Asset("issuer", "assetName");
        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(), List.of());
        OrderBook currentOrderBook = new OrderBook(43, "issuer", "assetName", List.of(), List.of());

        when(coreApiService.getCurrentTick()).thenReturn(Mono.just(43L));
        when(assetService.retrieveCurrentOrderBooks(43, Set.of(asset))).thenReturn(Flux.just(currentOrderBook));
        when(assetService.loadLatestOrderBooksBeforeTick(42, Set.of(asset))).thenReturn(Flux.just(previousOrderBook));
        List<AssetOrder> matchedOrders = List.of(mock(), mock());
        when(orderBookCalculator.getMatchedOrders(previousOrderBook, orderData, orderType)).thenReturn(matchedOrders);
        List<Trade> trades = List.of(mock(), mock());
        when(orderBookCalculator.handleTrades(transaction, Instant.EPOCH, matchedOrders, orderData, orderType)).thenReturn(trades);
        when(orderBookCalculator.updateOrderBooksWithTrades(previousOrderBook, transaction, orderType, orderData, matchedOrders, trades)).thenReturn(Tuples.of("issuer/assetName", previousOrderBook));

        StepVerifier.create(orderBookProcessor.calculateTrades(42, Instant.EPOCH, Set.of(asset), List.of(transaction)))
                .expectNext(trades)
                .verifyComplete();

    }

}