package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import org.qubic.qx.sync.repository.domain.TransactionMessage;
import org.qubic.qx.sync.repository.mapper.TransactionMessageMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class TransactionProcessor {

    private final TransactionRepository transactionRepository;
    private final TradeRepository tradeRepository;
    private final EventsProcessor eventsProcessor;
    private final TransactionMessageMapper transactionMessageMapper = new TransactionMessageMapper();

    public TransactionProcessor(TransactionRepository transactionRepository, TradeRepository tradeRepository, EventsProcessor eventsProcessor) {
        this.transactionRepository = transactionRepository;
        this.tradeRepository = tradeRepository;
        this.eventsProcessor = eventsProcessor;
    }

    public Mono<?> processQxTransaction(TransactionWithMeta transaction) {

        if (transaction.extraData() instanceof QxAssetOrderData orderData) {

            List<Trade> trades = eventsProcessor.calculateTrades(transaction, orderData);
            if (CollectionUtils.isNotEmpty(trades)) {
                // store transaction and trades
                return storeTransaction(transaction, true)
                        .then(storeTrades(trades));
            } else {
                // no relevant events but notify
                log.info("No trades found for transaction [{}].", transaction.transactionHash());
                return storeTransaction(transaction, false);

            }

        } else if (transaction.extraData() instanceof QxTransferAssetData) {

            if (eventsProcessor.isAssetTransferred(transaction.getEvents())) {
                return storeTransaction(transaction, true);
            } else {
                log.info("No asset transferred with transaction [{}]", transaction.transactionHash());
            }

        } else if (transaction.extraData() instanceof QxIssueAssetData) {

            if (eventsProcessor.isAssetIssued(transaction.getEvents())) {
                return storeTransaction(transaction, true);
            } else {
                log.info("No asset issued with transaction [{}]", transaction.transactionHash());
            }

        }

        return Mono.empty();

    }

    private Mono<TransactionMessage> storeTransaction(TransactionWithMeta tx, boolean relevantEvents) {
        TransactionMessage transactionMessage = transactionMessageMapper.map(tx.getTransaction(), tx.getTime(), relevantEvents);
        return transactionRepository.putTransactionIntoQueue(transactionMessage);
    }

    private Mono<List<Trade>> storeTrades(List<Trade> trades) {
        return Flux.fromIterable(trades)
                .flatMap(tradeRepository::putTradeIntoQueue)
                .collectList();
    }

}
