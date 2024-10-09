package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TransactionProcessor {

    private final CoreApiService coreService;
    private final AssetService assetService;
    private final OrderBookCalculator orderBookCalculator;
    private final TradeRepository tradeRepository;

    public TransactionProcessor(CoreApiService coreService, AssetService assetService, OrderBookCalculator orderBookCalculator, TradeRepository tradeRepository) {
        this.coreService = coreService;
        this.assetService = assetService;
        this.orderBookCalculator = orderBookCalculator;
        this.tradeRepository = tradeRepository;
    }

    public Mono<Void> updateAllOrderBooks() {
        return coreService.getCurrentTick()
                .flatMap(tick -> assetService
                        .retrieveAllCurrentOrderBooks(tick)
                        .then());
    }

    public Mono<List<Trade>> processQxOrders(long tickNumber, Instant tickTime, List<Transaction> txs) {

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
                .map(tuple -> handleQxOrderTransactions(tuple.getT1(), tuple.getT2(), orderTransactions))
                .flatMapMany(list -> storeTrades(list, tickTime))
                .collectList();

    }

    private Flux<Trade> storeTrades(List<Trade> list, Instant tickTime) {
        return Flux.fromIterable(list)
                .flatMap(trade -> tradeRepository.storeTrade(trade, tickTime));
    }

    private List<Trade> handleQxOrderTransactions(Set<OrderBook> currentObs, Set<OrderBook> previousObs, List<Transaction> orderTransactions) {

        Map<String, OrderBook> currentOrderBooks = currentObs.stream().collect(Collectors.toMap(TransactionProcessor::orderBookKey, x -> x));
        Map<String, OrderBook> previousOrderBooks = previousObs.stream().collect(Collectors.toMap(TransactionProcessor::orderBookKey, x -> x));

        List<Trade> detectedTrades = new ArrayList<>();

        // interpret orders
        for (Transaction tx : orderTransactions) {
            QxAssetOrderData orderData = (QxAssetOrderData) tx.extraData();
            OrderBook previousOrderBook = previousOrderBooks.get(orderBookKey(orderData));
            OrderBook currentOrderBook = currentOrderBooks.get(orderBookKey(orderData));

            OrderType orderType = getOrderType(tx);

            log.info("[{}]: {}", orderType, tx);
            log.info("Previous: {}", previousOrderBook);
            log.info("Current : {}.", currentOrderBook);

            if (previousOrderBook == null || currentOrderBook == null) {

                log.warn("Missing order book. Cannot calculate success.");
                // TODO we need to use money flew flag and/or logs

            } else {

                List<AssetOrder> matchedOrders = orderBookCalculator.getMatchedOrders(previousOrderBook, orderData, orderType);

                if (matchedOrders.isEmpty()) { // not trade triggered

                    log.info("No trade detected for order {}", orderData);
                    // not 100% accurate and not really necessary but good for debugging and if replaying older orders.
                    orderBookCalculator
                            .addOrdersToOrderBooks(previousOrderBook, tx, orderType, orderData)
                            .ifPresent(update -> {
                                OrderBook updatedOrderBook = update.getT2();
                                previousOrderBooks.put(update.getT1(), updatedOrderBook);
                                orderBookCalculator.compareCalculatedAndCurrentOrderBook(updatedOrderBook, currentOrderBook);
                            });

                } else {

                    List<Trade> trades = orderBookCalculator.handleTrades(tx, matchedOrders, orderData, orderType);
                    log.info("Detected [{}] trade(s) for order {}", trades.size(), orderData);
                    detectedTrades.addAll(trades);

                    Tuple2<String, OrderBook> update = orderBookCalculator.updateOrderBooksWithTrades(previousOrderBook, tx, orderType, orderData, matchedOrders, trades);
                    OrderBook updatedOrderBook = update.getT2();
                    previousOrderBooks.put(update.getT1(), updatedOrderBook);
                    orderBookCalculator.compareCalculatedAndCurrentOrderBook(updatedOrderBook, currentOrderBook);

                }

            }

        }

        return detectedTrades;
    }

    private static OrderType getOrderType(Transaction tx) {
        OrderType orderType = OrderType.fromCode(tx.inputType());
        assert orderType == OrderType.ADD_BID || orderType == OrderType.ADD_ASK || orderType == OrderType.REMOVE_BID || orderType == OrderType.REMOVE_ASK;
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

}
