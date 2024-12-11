package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.assets.AssetService;
import org.qubic.qx.sync.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class OrderBookProcessor {

    private final CoreApiService coreService;
    private final AssetService assetService;
    private final OrderBookCalculator orderBookCalculator;

    public OrderBookProcessor(CoreApiService coreService, AssetService assetService, OrderBookCalculator orderBookCalculator) {
        this.coreService = coreService;
        this.assetService = assetService;
        this.orderBookCalculator = orderBookCalculator;
    }

    public Flux<OrderBook> updateCurrentOrderBooks(long tickNumber, Set<Asset> relevantAssets) {
        return assetService.retrieveCurrentOrderBooks(tickNumber, relevantAssets);
    }

    public Mono<List<Trade>> calculateTrades(long tickNumber, Instant tickTime, Set<Asset> relevantAssets, List<Transaction> orderTransactions) {

        // only process transactions that could theoretically be successful trades
        List<Transaction> relevantTransactions = orderTransactions.stream()
                .filter(OrderBookProcessor::mightBeSuccessful)
                .toList();

        return coreService.getCurrentTick()
                .flatMapMany(tick -> updateCurrentOrderBooks(tick, relevantAssets))
                .collect(Collectors.toSet())
                .flatMap(currentOrderBooks -> assetService.loadLatestOrderBooksBeforeTick(tickNumber, relevantAssets)
                        .collect(Collectors.toSet())
                        .map(previous -> Tuples.of(currentOrderBooks, previous)))
                .map(tuple -> handleQxOrderTransactions(tuple.getT1(), tuple.getT2(), relevantTransactions, tickTime));
    }

    private List<Trade> handleQxOrderTransactions(Set<OrderBook> currentObs, Set<OrderBook> previousObs, List<Transaction> orderTransactions, Instant tickTime) {

        Map<String, OrderBook> currentOrderBooks = currentObs.stream().collect(Collectors.toMap(OrderBookProcessor::orderBookKey, x -> x));
        Map<String, OrderBook> previousOrderBooks = previousObs.stream().collect(Collectors.toMap(OrderBookProcessor::orderBookKey, x -> x));

        List<Trade> detectedTrades = new ArrayList<>();

        // interpret orders
        for (Transaction tx : orderTransactions) {

            QxAssetOrderData orderData = (QxAssetOrderData) tx.extraData();
            OrderBook previousOrderBook = previousOrderBooks.get(orderBookKey(orderData));
            OrderBook currentOrderBook = currentOrderBooks.get(orderBookKey(orderData));

            Qx.OrderType orderType = getOrderType(tx);

            log.info("[{}]: {}", orderType, tx);
            log.info("Previous: {}", previousOrderBook);
            log.info("Current : {}.", currentOrderBook);

            if (previousOrderBook == null || currentOrderBook == null) {

                // this should never happen. Only if synced without starting order book.
                log.warn("Missing order book. Cannot calculate success/trade.");

            } else {

                List<AssetOrder> matchedOrders = BooleanUtils.isFalse(tx.moneyFlew())
                        ? List.of() // no trade
                        : orderBookCalculator.getMatchedOrders(previousOrderBook, orderData, orderType);

                if (matchedOrders.isEmpty()) { // not trade triggered

                    if (orderType == Qx.OrderType.ADD_ASK && BooleanUtils.isTrue(tx.moneyFlew())) {

                        // this should not happen
                        log.error("Money flew but no trade detected for transaction [{}] with order data {}", tx.transactionHash(), orderData);

                    } else {

                        log.info("No trade detected for order {}", orderData);
                        // not 100% accurate and not really necessary but good for debugging and if replaying older orders.
                        orderBookCalculator
                                .addOrdersToOrderBooks(previousOrderBook, tx, orderType, orderData)
                                .ifPresent(update -> replacePreviousOrderBook(update, currentOrderBook, previousOrderBooks));

                    }

                } else {

                    List<Trade> trades = orderBookCalculator.handleTrades(tx, tickTime, matchedOrders, orderData, orderType);
                    log.info("Detected [{}] trade(s) for order {}", trades.size(), orderData);
                    detectedTrades.addAll(trades);

                    Tuple2<String, OrderBook> update = orderBookCalculator.updateOrderBooksWithTrades(previousOrderBook, tx, orderType, orderData, matchedOrders, trades);
                    replacePreviousOrderBook(update, currentOrderBook, previousOrderBooks);

                }

            }

        }

        return detectedTrades;
    }

    private void replacePreviousOrderBook(Tuple2<String, OrderBook> update, OrderBook currentOrderBook, Map<String, OrderBook> previousOrderBooks) {
        OrderBook updatedOrderBook = update.getT2();
        if (orderBookCalculator.compareCalculatedAndCurrentOrderBook(updatedOrderBook, currentOrderBook)) {
            previousOrderBooks.put(update.getT1(), updatedOrderBook);
        }
    }

    private static Qx.OrderType getOrderType(Transaction tx) {
        Qx.OrderType orderType = Qx.OrderType.fromCode(tx.inputType());
        assert orderType == Qx.OrderType.ADD_BID || orderType == Qx.OrderType.ADD_ASK || orderType == Qx.OrderType.REMOVE_BID || orderType == Qx.OrderType.REMOVE_ASK;
        return orderType;
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

    private static boolean mightBeSuccessful(Transaction tx) {
        //    money flew flag for successful transactions:
        //
        //    qx add bid -> true
        //    qx remove bid -> true
        //    qx add ask -> false - money flew does not help here
        //    qx remove ask -> false - money flew does not help here
        //    qx add ask with matching order  -> true

        final boolean potentiallySuccessful;
        if (tx.moneyFlew() == null) {
            log.info("Money flew flag not available.");
            potentiallySuccessful = true;
        } else {
            potentiallySuccessful = tx.moneyFlew() || isAskOrder(Qx.OrderType.fromCode(tx.inputType()));
            if (!potentiallySuccessful) {
                log.info("Ignoring transaction [{}] because of money flew flag.", tx.transactionHash());
            }
        }
        return potentiallySuccessful;
    }

    private static boolean isAskOrder(Qx.OrderType orderType) {
        return orderType == Qx.OrderType.REMOVE_ASK || orderType == Qx.OrderType.ADD_ASK;
    }


}
