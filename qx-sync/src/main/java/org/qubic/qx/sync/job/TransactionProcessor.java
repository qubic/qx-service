package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.mapper.TransactionMapper;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TransactionProcessor {

    private final TransactionRepository transactionRepository;
    private final TradeRepository tradeRepository;
    private final TransactionMapper transactionMapper;

    private final EventsProcessor eventsProcessor;
    private final OrderBookProcessor orderBookProcessor;


    public TransactionProcessor(TransactionRepository transactionRepository, TradeRepository tradeRepository, TransactionMapper transactionMapper, EventsProcessor eventsProcessor, OrderBookProcessor orderBookProcessor) {
        this.transactionRepository = transactionRepository;
        this.tradeRepository = tradeRepository;
        this.transactionMapper = transactionMapper;
        this.eventsProcessor = eventsProcessor;
        this.orderBookProcessor = orderBookProcessor;
    }

    public Mono<List<Trade>> processQxTransactions(long tickNumber, Instant tickTime, List<TransactionEvents> events, List<Transaction> txs) {

        // store all transactions
        Flux<TransactionWithTime> storeTransactions = storeTransactions(txs, tickTime);

        // process order transactions to detect trades
        List<Transaction> orderTransactions = txs.stream()
                .filter(tx -> tx.extraData() instanceof QxAssetOrderData)
                .toList();

        Set<Asset> relevantAssets = orderTransactions.stream()
                .map(tx -> (QxAssetOrderData) tx.extraData())
                .map(extra -> new Asset(extra.issuer(), extra.name()))
                .collect(Collectors.toSet());

        Mono<List<Trade>> findTradesMono = events.isEmpty() // no events available
                ? processWithOrderBooks(tickNumber, tickTime, relevantAssets, orderTransactions)
                : processWithEvents(tickNumber, tickTime, events, relevantAssets, orderTransactions);

        return storeTransactions
                .then(findTradesMono)
                .flatMap(this::storeTrades);

    }

    private Mono<List<Trade>> processWithEvents(long tickNumber, Instant tickTime, List<TransactionEvents> events, Set<Asset> relevantAssets, List<Transaction> orderTransactions) {
        return orderBookProcessor.updateCurrentOrderBooks(tickNumber, relevantAssets) // for following ticks
                .then(Mono.defer(() -> Mono.just(eventsProcessor.calculateTrades(tickNumber, tickTime, events, orderTransactions))));
    }

    private Mono<List<Trade>> processWithOrderBooks(long tickNumber, Instant tickTime, Set<Asset> relevantAssets, List<Transaction> orderTransactions) {
        return orderBookProcessor.calculateTrades(tickNumber, tickTime, relevantAssets, orderTransactions);
    }

    private Flux<TransactionWithTime> storeTransactions(List<Transaction> txs, Instant tickTime) {
        return Flux.fromIterable(txs)
                .map(tx -> transactionMapper.map(tx, tickTime.getEpochSecond()))
                .flatMap(transactionRepository::putTransactionIntoQueue);
    }

    private Mono<List<Trade>> storeTrades(List<Trade> trades) {
        return Flux.fromIterable(trades)
                .flatMap(tradeRepository::putTradeIntoQueue)
                .collectList();
    }

}
