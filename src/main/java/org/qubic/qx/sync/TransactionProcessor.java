package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx;
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
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class TransactionProcessor {

    private final CoreApiService coreService;
    private final AssetService assetService;
    private final TradeRepository tradeRepository;

    public TransactionProcessor(CoreApiService coreService, AssetService assetService, TradeRepository tradeRepository) {
        this.coreService = coreService;
        this.assetService = assetService;
        this.tradeRepository = tradeRepository;
    }

    public Mono<Void> updateAllOrderBooks() {
        return coreService.getCurrentTick()
                .flatMap(tick -> assetService
                        .retrieveAllCurrentOrderBooks(tick)
                        .then());
    }

    public Mono<Void> processQxOrders(Long tickNumber, Instant tickTime, List<Transaction> txs) {

        List<Transaction> orderTransactions = txs.stream()
                .filter(tx -> tx.extraData() instanceof QxAssetOrderData)
                .toList();

        Set<Asset> assetInformation = orderTransactions.stream()
                .map(tx -> (QxAssetOrderData) tx.extraData())
                .map(extra -> new Asset(extra.issuer(), extra.name()))
                .collect(Collectors.toSet());

        return coreService.getCurrentTick()
                .flatMapMany(tick -> assetService.retrieveCurrentOrderBooks(tick, assetInformation))
                .collect(Collectors.toSet())
                .flatMap(currentOrderBooks -> assetService.loadLatestOrderBooksBeforeTick(tickNumber, assetInformation)
                        .collect(Collectors.toSet())
                        .map(previous -> Tuples.of(currentOrderBooks, previous)))
                .map(tuple -> handleQxOrderTransactions(tuple.getT1(), tuple.getT1(), orderTransactions))
                .flatMapMany(list -> storeTrades(list, tickTime))
                .then();
    }

    private Flux<Trade> storeTrades(List<Trade> list, Instant tickTime) {
        return Flux.fromIterable(list)
                .flatMap(trade -> tradeRepository.storeTrade(trade, tickTime));
    }

    private static List<Trade> handleQxOrderTransactions(Set<OrderBook> currentObs, Set<OrderBook> previousObs, List<Transaction> orderTransactions) {
        Map<String, OrderBook> currentOrderBooks = currentObs.stream().collect(Collectors.toMap(TransactionProcessor::orderBookKey, x -> x));
        Map<String, OrderBook> previousOrderBooks = previousObs.stream().collect(Collectors.toMap(TransactionProcessor::orderBookKey, x -> x));

        List<Trade> detectedTrades = new ArrayList<>();

        // interpret orders
        for (Transaction tx : orderTransactions) {
            QxAssetOrderData orderData = (QxAssetOrderData) tx.extraData();
            OrderBook previousOrderBook = previousOrderBooks.get(orderBookKey(orderData));
            OrderBook currentOrderBook = currentOrderBooks.get(orderBookKey(orderData));
            Qx.Order orderType = Qx.Order.fromCode(tx.inputType());
            log.info("[{}]: {}", orderType, tx);
            log.info("Previous: {}", previousOrderBook);
            log.info("Current : {}.", currentOrderBook);

            if (previousOrderBook == null || currentOrderBook == null) {

                log.warn("Missing order book. Cannot calculate success.");
                // TODO we need to use money flew flag and/or logs

            } else {

                if (isRemoveOrder(orderType)) {

                    // cannot trigger trades but might change order book
                    List<AssetOrder> askOrders = isAskOrder(orderType) ? adaptAssetOrders(previousOrderBook.asks(), tx, orderType, orderData, false) : previousOrderBook.asks();
                    List<AssetOrder> bidOrders = isBidOrder(orderType) ? adaptAssetOrders(previousOrderBook.bids(), tx, orderType, orderData, false) : previousOrderBook.bids();
                    OrderBook calculatedOrderBook = calculateOrderBook(currentOrderBook, askOrders, bidOrders);
                    compareOrderBooks(previousOrderBook, calculatedOrderBook, currentOrderBook, previousOrderBooks, orderData);

                } else if (isAddOrder(orderType)) {

                    // trigger trades in case orders from the order book are matched
                    List<AssetOrder> potentialMatches = isBidOrder(orderType) ? previousOrderBook.asks() : previousOrderBook.bids();
                    List<AssetOrder> matchedOrders = potentialMatches.stream() // sorted from low to high
                            .takeWhile(new AssetOrderPredicate(orderData, isBidOrder(orderType)))
                            .toList();

                    if (matchedOrders.isEmpty()) { // add ask or unsuccessful order
                        // no trade
                        log.info("Add bid order to book: {}", orderData);
                        List<AssetOrder> askOrders = isAskOrder(orderType) ? adaptAssetOrders(previousOrderBook.asks(), tx, orderType, orderData, true) : previousOrderBook.asks();
                        List<AssetOrder> bidOrders = isBidOrder(orderType) ? adaptAssetOrders(previousOrderBook.bids(), tx, orderType, orderData, true) : previousOrderBook.bids();
                        OrderBook calculatedOrderBook = calculateOrderBook(currentOrderBook, askOrders, bidOrders);
                        compareOrderBooks(previousOrderBook, calculatedOrderBook, currentOrderBook, previousOrderBooks, orderData);
                    } else { // trade(s)
                        detectedTrades.addAll(handleTrades(tx, matchedOrders, orderData, isBidOrder(orderType)));
                    }

                } else { // unhandled case shouldn't happen

                    String message = String.format("Unexpected order type: [%s].", tx.inputType());
                    log.error(message);
                    throw new IllegalStateException(message);

                }
            }

        }

        return detectedTrades;
    }

    private static OrderBook calculateOrderBook(OrderBook currentOrderBook, List<AssetOrder> askOrders, List<AssetOrder> bidOrders) {
        return new OrderBook(currentOrderBook.tickNumber(), currentOrderBook.issuer(), currentOrderBook.assetName(), askOrders, bidOrders);
    }

    private static List<Trade> handleTrades(Transaction tx, List<AssetOrder> matchedOrders, QxAssetOrderData orderData, boolean bid) {
        // trade should happen
        List<Trade> trades = new ArrayList<>();
        long tradedShares = 0;
        for (AssetOrder matchedOrder : matchedOrders) {
            if (tradedShares < orderData.numberOfShares()) {
                long price = matchedOrder.price();
                long matchedShares = Math.min(matchedOrder.numberOfShares(), orderData.numberOfShares() - tradedShares);
                tradedShares += matchedShares;
                Trade trade = new Trade(tx.tick(), tx.transactionHash(), matchedOrder.entityId(), bid, orderData.issuer(), orderData.name(), matchedShares, price);
                log.info("Matched trade: {}. Offer: {}.", trade, matchedOrder);
                trades.add(trade);
            } else {
                log.debug("Skipping potential offer {} because order is already fulfilled.", matchedOrder);
            }
        }
        log.info("Trade(s) detected: {}", trades);
        return trades;
    }

    private static void compareOrderBooks(OrderBook previousOrderBook, OrderBook calculatedOrderBook, OrderBook currentOrderBook, Map<String, OrderBook> previousOrderBooks, QxAssetOrderData orderData) {
        if (calculatedOrderBook.equals(previousOrderBook)) {
            log.info("Order book did not change. Order data {}}.", orderData);
        } else { // try to calculate effect
            if (calculatedOrderBook.equals(currentOrderBook)) { // OK! as expected
                log.debug("Current order book matches calculated order book.");
                previousOrderBooks.put(orderBookKey(orderData), calculatedOrderBook);
            } else {
                log.warn("Calculated new order book differs from current order book.");
                log.info("Calculated order book: {}.", calculatedOrderBook);
            }
        }
    }

    private static List<AssetOrder> adaptAssetOrders(List<AssetOrder> previousOrders, Transaction tx, Qx.Order orderType, QxAssetOrderData orderData, boolean increaseShares) {
        List<AssetOrder> updatedOrders = previousOrders.stream()
                .map(ao -> assetOrderMatchesOrderData(tx, ao, orderData, increaseShares) ? createNewOrder(ao, orderData, increaseShares) : ao)
                .filter(ao -> ao.numberOfShares() > 0) // else remove completely
                .toList();

        if (previousOrders.equals(updatedOrders)) {
            log.info("No previous orders were updated.");
            if (orderType == Qx.Order.ADD_ASK || orderType == Qx.Order.ADD_BID) {
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

    private static int findIndexForInsert(List<AssetOrder> previousOrders, Qx.Order orderType, QxAssetOrderData orderData) {
        for (int i = 0; i < previousOrders.size(); i++) {
            if (orderType == Qx.Order.ADD_BID && previousOrders.get(i).price() >= orderData.price()) {
                return i;
            } else if (orderType == Qx.Order.ADD_ASK && previousOrders.get(i).price() > orderData.price()) {
                return i;
            }
        }
        return previousOrders.size();
    }

    private static boolean assetOrderMatchesOrderData(Transaction tx, AssetOrder ao, QxAssetOrderData orderData, boolean increaseShares) {
        return StringUtils.equals(ao.entityId(), tx.sourcePublicId())
                && ao.price() == orderData.price()
                && (increaseShares || ao.numberOfShares() >= orderData.numberOfShares());
    }

    private static AssetOrder createNewOrder(AssetOrder old, QxAssetOrderData orderData, boolean increaseShares) {
        AssetOrder assetOrder = new AssetOrder(old.entityId(), old.price(), old.numberOfShares() + (orderData.numberOfShares() * (increaseShares ? 1 : -1)));
        log.debug("Calculated new order {}. Old: {}, order data: {}.", assetOrder, old, orderData);
        return assetOrder;
    }

    private static String orderBookKey(QxAssetOrderData orderData) {
        return orderBookKey(orderData.issuer(), orderData.name());
    }

    private static String orderBookKey(OrderBook x) {
        return orderBookKey(x.issuer(), x.assetName());
    }

    private static String orderBookKey(String issuer, String assetName) {
        return String.format("%s/%s", issuer, assetName);
    }

    private static boolean isAskOrder(Qx.Order orderType) {
        return orderType == Qx.Order.REMOVE_ASK || orderType == Qx.Order.ADD_ASK;
    }

    private static boolean isBidOrder(Qx.Order orderType) {
        return orderType == Qx.Order.REMOVE_BID || orderType == Qx.Order.ADD_BID;
    }

    private static boolean isAddOrder(Qx.Order orderType) {
        return orderType == Qx.Order.ADD_BID || orderType == Qx.Order.ADD_ASK;
    }

    private static boolean isRemoveOrder(Qx.Order orderType) {
        return orderType == Qx.Order.REMOVE_BID || orderType == Qx.Order.REMOVE_ASK;
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
