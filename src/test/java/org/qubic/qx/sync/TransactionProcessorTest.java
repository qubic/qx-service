package org.qubic.qx.sync;

import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx.OrderType;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.assets.Asset;
import org.qubic.qx.assets.AssetService;
import org.qubic.qx.domain.OrderBook;
import org.qubic.qx.domain.QxAssetOrderData;
import org.qubic.qx.domain.Trade;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.TradeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionProcessorTest {
    private final CoreApiService coreApiService = mock();
    private final AssetService assetService = mock();
    private final OrderBookCalculator orderBookCalculator = mock();
    private final TradeRepository tradeRepository = mock();
    private final TransactionProcessor transactionProcessor = new TransactionProcessor(coreApiService, assetService, orderBookCalculator, tradeRepository);

    @Test
    void processQxOrders_thenStoreTrades() {

        OrderType orderType = OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "assetName", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData);
        Asset asset = new Asset("issuer", "assetName");
        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(), List.of());
        OrderBook currentOrderBook = new OrderBook(43, "issuer", "assetName", List.of(), List.of());

        when(coreApiService.getCurrentTick()).thenReturn(Mono.just(43L));
        when(assetService.retrieveCurrentOrderBooks(43, Set.of(asset))).thenReturn(Flux.just(currentOrderBook));
        when(assetService.loadLatestOrderBooksBeforeTick(42, Set.of(asset))).thenReturn(Flux.just(previousOrderBook));
        List<AssetOrder> matchedOrders = List.of(mock(), mock());
        when(orderBookCalculator.getMatchedOrders(previousOrderBook, orderData, orderType)).thenReturn(matchedOrders);
        List<Trade> trades = List.of(mock(), mock());
        when(orderBookCalculator.handleTrades(transaction, matchedOrders, orderData, orderType)).thenReturn(trades);
        when(tradeRepository.storeTrade(any(Trade.class), any(Instant.class))).then(args -> Mono.just(args.getArgument(0)));
        when(orderBookCalculator.updateOrderBooksWithTrades(previousOrderBook, transaction, orderType, orderData, matchedOrders, trades)).thenReturn(Tuples.of("issuer/assetName", previousOrderBook));

        StepVerifier.create(transactionProcessor.processQxOrders(42, Instant.EPOCH, List.of(transaction)))
                .expectNext(trades)
                .verifyComplete();

    }

}