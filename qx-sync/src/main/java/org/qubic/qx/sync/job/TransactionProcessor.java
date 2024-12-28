package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class TransactionProcessor {

    private final TransactionRepository transactionRepository;
    private final TradeRepository tradeRepository;
    private final EventsProcessor eventsProcessor;

    public TransactionProcessor(TransactionRepository transactionRepository, TradeRepository tradeRepository, EventsProcessor eventsProcessor) {
        this.transactionRepository = transactionRepository;
        this.tradeRepository = tradeRepository;
        this.eventsProcessor = eventsProcessor;
    }

    public Mono<?> processQxTransaction(TransactionWithTime transaction, List<TransactionEvent> events) {

        Mono<TransactionWithTime> storeTransactionMono = Mono.defer(() -> storeTransaction(transaction));

        if (transaction.extraData() instanceof QxAssetOrderData orderData) {

            List<Trade> trades = eventsProcessor.calculateTrades(transaction, events, orderData);

            if (CollectionUtils.isNotEmpty(trades)) {

                // if there is a trade the transaction and the trade should get stored
                return storeTransactionMono
                        .then(storeTrades(trades))
                        .then(Mono.just(transaction));

            } else {

                // don't store anything but notify that there was a transaction (for clearing caches)
                log.info("No trades found for transaction [{}].", transaction.transactionHash());
                // FIXME inform about unsuccessful transaction

            }

        } else if (transaction.extraData() instanceof QxTransferAssetData) { // TODO add test

            if (eventsProcessor.isAssetTransferred(events)) {
                return storeTransactionMono
                        .then(Mono.just(transaction));
            } else {
                log.info("No asset transferred with transaction [{}]", transaction.transactionHash());
            }

        } else if (transaction.extraData() instanceof QxIssueAssetData) { // TODO add test

            if (eventsProcessor.isAssetIssued(events)) {
                return storeTransactionMono
                        .then(Mono.just(transaction));
            } else {
                log.info("No asset issued with transaction [{}]", transaction.transactionHash());
            }

        }

        return Mono.just(transaction);

    }

    private Mono<TransactionWithTime> storeTransaction(TransactionWithTime tx) {
        return transactionRepository.putTransactionIntoQueue(tx);
    }

    private Mono<List<Trade>> storeTrades(List<Trade> trades) {
        return Flux.fromIterable(trades)
                .flatMap(tradeRepository::putTradeIntoQueue)
                .collectList();
    }

}
