package org.qubic.qx.sync.job;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.domain.AssetOrder;
import org.qubic.qx.sync.domain.OrderBook;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.Trade;
import org.qubic.qx.sync.domain.Transaction;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookCalculatorTest {

    private final OrderBookCalculator calculator = new OrderBookCalculator();

    @Test
    void getMatchedOrders_givenBidOrderType() {

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "assetName", 5, 5);

        AssetOrder prevAsk1 = new AssetOrder("entity1",4,1);
        AssetOrder prevAsk2 = new AssetOrder("entity2",5,2);
        AssetOrder prevAsk3 = new AssetOrder("entity3",6,10);

        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2, prevAsk3), List.of());

        List<AssetOrder> matchedOrders = calculator.getMatchedOrders(previousOrderBook, orderData, orderType);
        assertThat(matchedOrders).hasSize(2);
        assertThat(matchedOrders).containsExactly(prevAsk1, prevAsk2);

    }

    @Test
    void getMatchedOrders_givenAskOrderType() {

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "assetName", 4, 5);

        AssetOrder bid1 = new AssetOrder("entity1", 3, 2);
        AssetOrder bid2 = new AssetOrder("entity2", 4, 2);
        AssetOrder bid3 = new AssetOrder("entity3", 5, 2);
        AssetOrder bid4 = new AssetOrder("entity4", 6, 2);

        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(), List.of(bid4, bid3, bid2, bid1));

        List<AssetOrder> matchedOrders = calculator.getMatchedOrders(previousOrderBook, orderData, orderType);
        assertThat(matchedOrders).containsExactly(bid4, bid3, bid2);

    }

    @Test
    void handleTrades_givenBidOrderType() {

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        AssetOrder ask1 = new AssetOrder("entity1",4,2);
        AssetOrder ask2 = new AssetOrder("entity2",5,5);

        List<Trade> trades = calculator.handleTrades(transaction, Instant.EPOCH, List.of(ask1, ask2), orderData, orderType);
        assertThat(trades).hasSize(2);
        assertThat(trades).containsExactly(
                new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 4, 2),
                new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity2", "issuer", "asset", 5, 3)
        );

    }

    @Test
    void handleTrades_givenAskOrderType() {

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 1, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        AssetOrder bid1 = new AssetOrder("entity1",4,2);
        AssetOrder bid2 = new AssetOrder("entity2",5,4);

        List<Trade> trades = calculator.handleTrades(transaction, Instant.EPOCH, List.of(bid2, bid1), orderData, orderType);
        assertThat(trades).hasSize(2);
        assertThat(trades).containsExactly(
                new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", false, "sourceId", "entity2", "issuer", "asset", 5, 4),
                new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", false, "sourceId", "entity1", "issuer", "asset", 4, 1)
        );

    }

    @Test
    void addOrdersToOrderBooks_givenNewBid_thenAddNewEntry() {

        AssetOrder prevBid1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevBid2 = new AssetOrder("entity2", 5, 1);
        AssetOrder prevBid3 = new AssetOrder("entity3", 3, 1);

        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(), List.of(prevBid1, prevBid2, prevBid3));

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        AssetOrder newBid = new AssetOrder("sourceId", 5, 5);
        Optional<Tuple2<String, OrderBook>> changedOrderBook = calculator.addOrdersToOrderBooks(previousOrderBook, transaction, orderType, orderData);

        assertThat(changedOrderBook).isPresent();
        assertThat(changedOrderBook.get().getT1()).isEqualTo("issuer/asset");
        assertThat(changedOrderBook.get().getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(), List.of(prevBid1, prevBid2, newBid, prevBid3)));

    }

    @Test
    void addOrdersToOrderBooks_givenAddToBid_thenModifyEntry() {

        AssetOrder prevBid1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevBid2 = new AssetOrder("entity2", 5, 1);
        AssetOrder prevBid3 = new AssetOrder("entity3", 3, 1);

        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(), List.of(prevBid1, prevBid2, prevBid3));

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "entity2", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        AssetOrder combinedNewBid = new AssetOrder("entity2", 5, 6);
        Optional<Tuple2<String, OrderBook>> changedOrderBook = calculator.addOrdersToOrderBooks(previousOrderBook, transaction, orderType, orderData);

        assertThat(changedOrderBook).isPresent();
        assertThat(changedOrderBook.get().getT1()).isEqualTo("issuer/asset");
        assertThat(changedOrderBook.get().getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(), List.of(prevBid1, combinedNewBid, prevBid3)));

    }

    @Test
    void addOrdersToOrderBooks_givenNewAsk_thenAddNewEntry() {

        AssetOrder prevAsk1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevAsk2 = new AssetOrder("entity2", 5, 1);
        AssetOrder prevAsk3 = new AssetOrder("entity3", 7, 1);

        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2, prevAsk3), List.of());

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        AssetOrder newAsk = new AssetOrder("sourceId", 5, 5);
        Optional<Tuple2<String, OrderBook>> changedOrderBook = calculator.addOrdersToOrderBooks(previousOrderBook, transaction, orderType, orderData);

        assertThat(changedOrderBook).isPresent();
        assertThat(changedOrderBook.get().getT1()).isEqualTo("issuer/asset");
        assertThat(changedOrderBook.get().getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2, newAsk, prevAsk3), List.of()));

    }

    @Test
    void addOrdersToOrderBooks_givenAddToAsk_thenModifyEntry() {

        AssetOrder prevAsk1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevAsk2 = new AssetOrder("entity2", 5, 1);
        AssetOrder prevAsk3 = new AssetOrder("entity3", 7, 1);

        OrderBook previousOrderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2, prevAsk3), List.of());

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "entity2", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        AssetOrder newAsk = new AssetOrder("entity2", 5, 6);
        Optional<Tuple2<String, OrderBook>> changedOrderBook = calculator.addOrdersToOrderBooks(previousOrderBook, transaction, orderType, orderData);

        assertThat(changedOrderBook).isPresent();
        assertThat(changedOrderBook.get().getT1()).isEqualTo("issuer/asset");
        assertThat(changedOrderBook.get().getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, newAsk, prevAsk3), List.of()));

    }

    @Test
    void updateOrderBookWithTrades_givenBid_thenEmptyOrderBook() { // test strip to empty lists
        AssetOrder prevAsk1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevAsk2 = new AssetOrder("entity2", 5, 4);
        OrderBook orderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2), List.of());

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        List<AssetOrder> matchedOrders = List.of(prevAsk1, prevAsk2);
        Trade trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 5, 1);
        Trade trade2 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity2", "issuer", "asset", 5, 4);
        List<Trade> trades = List.of(trade1, trade2);

        Tuple2<String, OrderBook> updateOrderBooksWithTrades = calculator.updateOrderBooksWithTrades(orderBook, transaction, orderType, orderData, matchedOrders, trades);
        assertThat(updateOrderBooksWithTrades.getT1()).isEqualTo("issuer/asset");
        assertThat(updateOrderBooksWithTrades.getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(), List.of())); // all sold
    }

    @Test
    void updateOrderBookWithTrades_givenAsk_thenEmptyOrderBook() { // test strip to empty lists
        AssetOrder prevBid = new AssetOrder("entity1", 5, 2);
        OrderBook orderBook = new OrderBook(41, "issuer", "assetName", List.of(), List.of(prevBid));

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 2);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        List<AssetOrder> matchedOrders = List.of(prevBid);
        Trade trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 5, 2);
        List<Trade> trades = List.of(trade1);

        Tuple2<String, OrderBook> updateOrderBooksWithTrades = calculator.updateOrderBooksWithTrades(orderBook, transaction, orderType, orderData, matchedOrders, trades);
        assertThat(updateOrderBooksWithTrades.getT1()).isEqualTo("issuer/asset");
        assertThat(updateOrderBooksWithTrades.getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(), List.of()));
    }

    @Test
    void updateOrderBookWithTrades_givenBidCompletelyFulfilled_thenRemoveMatchingAsks() {
        AssetOrder prevAsk1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevAsk2 = new AssetOrder("entity2", 5, 4);
        AssetOrder prevAsk3 = new AssetOrder("entity3", 6, 1);
        AssetOrder prevBid = new AssetOrder("entity4", 4, 1);

        OrderBook orderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2, prevAsk3), List.of(prevBid));

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        List<AssetOrder> matchedOrders = List.of(prevAsk1, prevAsk2);
        Trade trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 5, 1);
        Trade trade2 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity2", "issuer", "asset", 5, 4);
        List<Trade> trades = List.of(trade1, trade2);

        Tuple2<String, OrderBook> updateOrderBooksWithTrades = calculator.updateOrderBooksWithTrades(orderBook, transaction, orderType, orderData, matchedOrders, trades);
        assertThat(updateOrderBooksWithTrades.getT1()).isEqualTo("issuer/asset");
        assertThat(updateOrderBooksWithTrades.getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(prevAsk3), List.of(prevBid)));
    }

    @Test
    void updateOrderBookWithTrades_givenAskCompletelyFulfilled_thenRemoveMatchingBids() {
        AssetOrder prevBid1 = new AssetOrder("entity1", 5, 2);
        AssetOrder prevBid2 = new AssetOrder("entity2", 5, 3);
        AssetOrder prevBid3 = new AssetOrder("entity3", 4, 1);
        AssetOrder prevAsk = new AssetOrder("entity4", 7, 10);
        OrderBook orderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk), List.of(prevBid1, prevBid2, prevBid3));

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        List<AssetOrder> matchedOrders = List.of(prevBid1, prevBid2);
        Trade trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 5, 2);
        Trade trade2 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity2", "issuer", "asset", 5, 3);
        List<Trade> trades = List.of(trade1, trade2);

        Tuple2<String, OrderBook> updateOrderBooksWithTrades = calculator.updateOrderBooksWithTrades(orderBook, transaction, orderType, orderData, matchedOrders, trades);
        assertThat(updateOrderBooksWithTrades.getT1()).isEqualTo("issuer/asset");
        assertThat(updateOrderBooksWithTrades.getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(prevAsk), List.of(prevBid3)));
    }

    @Test
    void updateOrderBookWithTrades_givenBidPartiallyFulfilled_thenRemoveAsksAndAddBid() {
        AssetOrder prevAsk1 = new AssetOrder("entity1", 5, 1);
        AssetOrder prevAsk2 = new AssetOrder("entity2", 5, 1);
        AssetOrder prevAsk3 = new AssetOrder("entity3", 7, 1);
        OrderBook orderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk1, prevAsk2, prevAsk3), List.of());

        Qx.OrderType orderType = Qx.OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        List<AssetOrder> matchedOrders = List.of(prevAsk1, prevAsk2);
        Trade trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 5, 1);
        Trade trade2 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity2", "issuer", "asset", 5, 1);
        List<Trade> trades = List.of(trade1, trade2);

        Tuple2<String, OrderBook> updateOrderBooksWithTrades = calculator.updateOrderBooksWithTrades(orderBook, transaction, orderType, orderData, matchedOrders, trades);
        assertThat(updateOrderBooksWithTrades.getT1()).isEqualTo("issuer/asset");
        assertThat(updateOrderBooksWithTrades.getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(prevAsk3), List.of(new AssetOrder("sourceId", 5, 3))));
    }

    @Test
    void updateOrderBookWithTrades_givenAskPartiallyFulfilled_thenRemoveBidsAndAddAsk() {
        AssetOrder prevBid1 = new AssetOrder("entity1", 5, 2);
        AssetOrder prevBid2 = new AssetOrder("entity2", 5, 2);
        AssetOrder prevBid3 = new AssetOrder("entity3", 4, 1);
        AssetOrder prevAsk = new AssetOrder("entity4", 7, 10);
        OrderBook orderBook = new OrderBook(41, "issuer", "assetName", List.of(prevAsk), List.of(prevBid1, prevBid2, prevBid3));

        Qx.OrderType orderType = Qx.OrderType.ADD_ASK;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        Transaction transaction = new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);

        List<AssetOrder> matchedOrders = List.of(prevBid1, prevBid2);
        Trade trade1 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity1", "issuer", "asset", 5, 2);
        Trade trade2 = new Trade(42, Instant.EPOCH.getEpochSecond(), "hash", true, "sourceId", "entity2", "issuer", "asset", 5, 2);
        List<Trade> trades = List.of(trade1, trade2);

        Tuple2<String, OrderBook> updateOrderBooksWithTrades = calculator.updateOrderBooksWithTrades(orderBook, transaction, orderType, orderData, matchedOrders, trades);
        assertThat(updateOrderBooksWithTrades.getT1()).isEqualTo("issuer/asset");
        assertThat(updateOrderBooksWithTrades.getT2()).isEqualTo(new OrderBook(41, "issuer", "assetName", List.of(new AssetOrder("sourceId", 5, 1), prevAsk), List.of(prevBid3)));
    }

}