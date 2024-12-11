package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.domain.AssetOrder;
import org.qubic.qx.sync.domain.OrderBook;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.Trade;
import org.qubic.qx.sync.domain.Transaction;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Slf4j
public class OrderBookCalculator {

    /**
     * Returns the offers that are matched by the order.
     * @param orderBook The offers from the order book are to be checked for matches.
     * @param orderData The data of the order.
     * @param orderType The type of the order to determine, if it matches offers from the order book. Only add order can
     *                  match. Bid/ask orders have different selection logic.
     * @return The List of offers that are consumed by the order. Empty list, if there are no matches.
     */
    public List<AssetOrder> getMatchedOrders(OrderBook orderBook, QxAssetOrderData orderData, Qx.OrderType orderType) {
        if (isAddOrder(orderType)) {
            boolean isBid = isBidOrder(orderType);
            List<AssetOrder> offers = isBid ? orderBook.asks() : orderBook.bids();
            return offers.stream()
                    .takeWhile(new AssetOrderPredicate(orderData, isBid))
                    .toList();
        } else {
            return List.of();
        }
    }

    public List<Trade> handleTrades(Transaction tx, Instant tickTime, List<AssetOrder> matchedOrders, QxAssetOrderData orderData, Qx.OrderType orderType) {
        // trade should happen
        List<Trade> trades = new ArrayList<>();
        long tradedShares = 0;
        for (AssetOrder matchedOrder : matchedOrders) {
            if (tradedShares < orderData.numberOfShares()) {
                long price = matchedOrder.price();
                long matchedShares = Math.min(matchedOrder.numberOfShares(), orderData.numberOfShares() - tradedShares);
                tradedShares += matchedShares;
                Trade trade = new Trade(tx.tick(), tickTime.getEpochSecond(), tx.transactionHash(), isBidOrder(orderType), tx.sourcePublicId(), matchedOrder.entityId(), orderData.issuer(), orderData.name(), price, matchedShares);
                log.info("Matched trade: {}. Offer: {}.", trade, matchedOrder);
                trades.add(trade);
            } else {
                log.debug("Skipping potential offer {} because order is already fulfilled.", matchedOrder);
            }
        }
        return trades;
    }

    /**
     * Recalculate previous order book for order that did not trigger trade.
     *
     * @param previousOrderBook The previous order book that might get changed.
     * @param tx                Transaction triggering the trade (for source public identity).
     * @param orderType         The type of the order.
     * @param orderData         The new order.
     * @return The updated order book in case the previous book changed.
     */
    public Optional<Tuple2<String, OrderBook>> addOrdersToOrderBooks(OrderBook previousOrderBook, Transaction tx, Qx.OrderType orderType, QxAssetOrderData orderData) {
        List<AssetOrder> askOrders = isAskOrder(orderType) ? updateOffersWithOrder(previousOrderBook.asks(), tx, orderType, orderData) : previousOrderBook.asks();
        List<AssetOrder> bidOrders = isBidOrder(orderType) ? updateOffersWithOrder(previousOrderBook.bids(), tx, orderType, orderData) : previousOrderBook.bids();
        OrderBook calculatedOrderBook = new OrderBook(previousOrderBook.tickNumber(), previousOrderBook.issuer(), previousOrderBook.assetName(), askOrders, bidOrders);
        return calculatedOrderBook.equals(previousOrderBook)
                ? Optional.empty()
                : Optional.of(Tuples.of(orderBookKey(orderData), calculatedOrderBook)); // no update needed
    }

    public boolean compareCalculatedAndCurrentOrderBook(OrderBook calculatedOrderBook, OrderBook currentOrderBook) {
        // order book changed
        OrderBook forCompareOnly = new OrderBook(currentOrderBook.tickNumber(), calculatedOrderBook.issuer(),
                calculatedOrderBook.assetName(), calculatedOrderBook.asks(), calculatedOrderBook.bids());
        if (forCompareOnly.equals(currentOrderBook)) { // OK! equal to current order book. Should match at end of tick.
            log.debug("Current order book matches calculated order book.");
            return true;
        } else {
            // can happen if there are multiple orders or if calculation failed or if transaction did not execute successfully
            log.warn("Calculated new order book differs from current order book.");
            log.info("Calculated order book: {}.", calculatedOrderBook);
            return false;
        }
    }

    public Tuple2<String, OrderBook> updateOrderBooksWithTrades(OrderBook previousOrderBook, Transaction tx, Qx.OrderType orderType, QxAssetOrderData orderData, List<AssetOrder> matchedOrders, List<Trade> trades) {

        long requestedShares = orderData.numberOfShares();
        long tradedShares = trades.stream().mapToLong(Trade::numberOfShares).sum();
        long availableShares = matchedOrders.stream().mapToLong(AssetOrder::numberOfShares).sum();
        log.debug("Requested shares [{}], traded shares [{}], available shares [{}].", requestedShares, tradedShares, availableShares);

        boolean isBid = isBidOrder(orderType);
        List<AssetOrder> offers = isBid ? previousOrderBook.asks() : previousOrderBook.bids();
        List<AssetOrder> newOffers = IntStream.range(0, offers.size())
                .filter(i -> i >= trades.size() - 1) // remove certain matches
                .mapToObj(i -> {
                    if (availableShares >= tradedShares && i == trades.size() - 1) { // modify last offer
                        AssetOrder offer = offers.get(i);
                        AssetOrder newOffer = new AssetOrder(offer.entityId(), offer.price(), offer.numberOfShares() - trades.get(i).numberOfShares());
                        log.debug("Modify offer {} because of trade {}: {}", offer, trades.get(i), newOffer);
                        return newOffer;
                    } else {
                        log.debug("Unchanged offer at index [{}]: {}", i, offers.get(i));
                        return offers.get(i);
                    }
                })
                .filter(offer -> offer.numberOfShares() > 0) // eliminate last offer in case all shares were traded
                .toList();


        OrderBook newOrderBook = new OrderBook(previousOrderBook.tickNumber(), previousOrderBook.issuer(), previousOrderBook.assetName(), isBid ? newOffers : previousOrderBook.asks(), isBid ? previousOrderBook.bids() : newOffers);
        if (requestedShares > tradedShares) { // add new order
            return addOrdersToOrderBooks(newOrderBook, tx, orderType, new QxAssetOrderData(orderData.issuer(), orderData.name(), orderData.price(), orderData.numberOfShares() - tradedShares))
                    .orElseThrow(); // should never happen as we need to modify the order book
        } else { // no orders to add
            return Tuples.of(orderBookKey(orderData), newOrderBook);
        }

    }

    /**
     * Update the offers with a new order (add or remove shares to/from it).
     * @param previousOrders The previous offers.
     * @param tx The transaction with the new order.
     * @param orderType The type of the new order.
     * @param orderData The new order.
     * @return The updated offers. If no update happened they are unchanged.
     */
    private static List<AssetOrder> updateOffersWithOrder(List<AssetOrder> previousOrders, Transaction tx, Qx.OrderType orderType, QxAssetOrderData orderData) {
        List<AssetOrder> updatedOrders = previousOrders.stream()
                .map(ao -> assetOrderMatchesOrderData(tx, ao, orderData, isAddOrder(orderType)) ? createNewOrder(ao, orderData, isAddOrder(orderType)) : ao)
                .filter(ao -> ao.numberOfShares() > 0) // else remove completely
                .toList();

        if (previousOrders.equals(updatedOrders)) {
            log.debug("No previous orders were updated when inserting.");
            if (isAddOrder(orderType)) {
                List<AssetOrder> withAddedOrder = new ArrayList<>(previousOrders);
                int index = findIndexForInsert(previousOrders, orderType, orderData);
                AssetOrder assetOrder = new AssetOrder(tx.sourcePublicId(), orderData.price(), orderData.numberOfShares());
                log.debug("Insert new order at index [{}]. [{}]: {}", index, orderType, assetOrder);
                withAddedOrder.add(index, assetOrder);
                return withAddedOrder;
            }
        }

        return updatedOrders;
    }


    private static boolean assetOrderMatchesOrderData(Transaction tx, AssetOrder ao, QxAssetOrderData orderData, boolean increaseShares) {
        return StringUtils.equals(ao.entityId(), tx.sourcePublicId())
                && ao.price() == orderData.price()
                && (increaseShares || ao.numberOfShares() >= orderData.numberOfShares());
    }

    private static AssetOrder createNewOrder(AssetOrder old, QxAssetOrderData orderData, boolean increaseShares) {
        AssetOrder assetOrder = new AssetOrder(old.entityId(), old.price(), old.numberOfShares() + (orderData.numberOfShares() * (increaseShares ? 1 : -1)));
        log.debug("Update order. New {}, old {}, input {}.", assetOrder, old, orderData);
        return assetOrder;
    }

    private static int findIndexForInsert(List<AssetOrder> previousOrders, Qx.OrderType orderType, QxAssetOrderData orderData) {
        for (int i = 0; i < previousOrders.size(); i++) {
            if (orderType == Qx.OrderType.ADD_BID && previousOrders.get(i).price() < orderData.price()) {
                return i;
            } else if (orderType == Qx.OrderType.ADD_ASK && previousOrders.get(i).price() > orderData.price()) {
                return i;
            }
        }
        return previousOrders.size();
    }

    private static String orderBookKey(QxAssetOrderData orderData) {
        return orderBookKey(orderData.issuer(), orderData.name());
    }

    private static String orderBookKey(String issuer, String assetName) {
        return String.format("%s/%s", issuer, assetName);
    }

    private static boolean isAddOrder(Qx.OrderType orderType) {
        return orderType == Qx.OrderType.ADD_BID || orderType == Qx.OrderType.ADD_ASK;
    }

    private static boolean isAskOrder(Qx.OrderType orderType) {
        return orderType == Qx.OrderType.REMOVE_ASK || orderType == Qx.OrderType.ADD_ASK;
    }

    private static boolean isBidOrder(Qx.OrderType orderType) {
        return orderType == Qx.OrderType.REMOVE_BID || orderType == Qx.OrderType.ADD_BID;
    }

    private static class AssetOrderPredicate implements Predicate<AssetOrder> {
        private final QxAssetOrderData orderData;
        private final boolean buy;
        long shares;

        public AssetOrderPredicate(QxAssetOrderData orderData, boolean bid) {
            this.orderData = orderData;
            this.buy = bid; // bid == buy, ask == sell
            shares = orderData.numberOfShares();
        }

        @Override
        public boolean test(AssetOrder assetOrder) {
            if (buy && assetOrder.price() <= orderData.price() && shares > 0) {
                shares -= assetOrder.numberOfShares();
                return true;
            } if (!buy && assetOrder.price() >= orderData.price() && shares > 0) {
                shares += assetOrder.numberOfShares();
                return true;
            }
            else {
                return false;
            }
        }
    }

}
