package org.qubic.qx.sync.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.Qx.OrderType;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.mapper.TransactionMapper;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

class TransactionProcessorTest {

    private final TransactionRepository transactionRepository = mock();
    private final TradeRepository tradeRepository = mock();
    private final TransactionMapper transactionMapper = mock();
    private final EventsProcessor eventsProcessor = mock();
    private final OrderBookProcessor orderBookProcessor = mock();
    private final TransactionProcessor transactionProcessor = new TransactionProcessor(transactionRepository, tradeRepository, transactionMapper, eventsProcessor, orderBookProcessor);


    @BeforeEach
    void initMocks() {
        when(tradeRepository.putTradeIntoQueue(any(Trade.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        TransactionWithTime storedTransaction = mock();
        when(transactionMapper.map(any(Transaction.class), anyLong())).thenReturn(storedTransaction);
        when(transactionRepository.putTransactionIntoQueue(any(TransactionWithTime.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void processQxTransactions_thenPutTransactionIntoQueue() {
        Transaction transaction = testTransaction();
        when(orderBookProcessor.calculateTrades(anyLong(), any(), any(), any())).thenReturn(Mono.just(List.of()));

        StepVerifier.create(transactionProcessor.processQxTransactions(42, Instant.EPOCH, List.of(), List.of(transaction)))
                .expectNext(List.of())
                .verifyComplete();

        verify(transactionRepository).putTransactionIntoQueue(any(TransactionWithTime.class));
    }

    @Test
    void processQxTransactions_thenStoreTrades() {
        Transaction transaction = testTransaction();
        Trade trade = mock();
        when(orderBookProcessor.calculateTrades(anyLong(), any(), any(), any())).thenReturn(Mono.just(List.of(trade)));

        StepVerifier.create(transactionProcessor.processQxTransactions(42, Instant.EPOCH, List.of(), List.of(transaction)))
                .expectNext(List.of(trade))
                .verifyComplete();

        verify(tradeRepository).putTradeIntoQueue(trade);
    }

    @Test
    void processQxTransactions_givenEvents_thenCallEventsProcessor() {
        Transaction transaction = testTransaction();
        TransactionEvents transactionEvents = mock();

        Trade trade = mock();
        when(orderBookProcessor.updateCurrentOrderBooks(anyLong(), anySet())).thenReturn(Flux.empty());
        when(eventsProcessor.calculateTrades(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction))).thenReturn(List.of(trade));

        StepVerifier.create(transactionProcessor.processQxTransactions(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction)).log())
                .expectNext(List.of(trade))
                .verifyComplete();
    }

    @Test
    void processQxTransactions_givenOtherThransactions_thenProcessOrdersOnly() {
        Transaction transaction = testTransaction();
        Transaction otherTransaction = new Transaction("other-hash", "source", "destination", 1, 42, OrderType.TRANSFER_SHARE.code, 0, new QxTransferAssetData("issuer", "asset", "new-owner", 1), null);
        TransactionEvents transactionEvents = mock();

        Trade trade = mock();
        when(orderBookProcessor.updateCurrentOrderBooks(anyLong(), anySet())).thenReturn(Flux.empty());
        when(eventsProcessor.calculateTrades(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction))).thenReturn(List.of(trade)); // only process order

        StepVerifier.create(transactionProcessor.processQxTransactions(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction, otherTransaction)).log())
                .expectNext(List.of(trade))
                .verifyComplete();

        verify(transactionRepository, times(2)).putTransactionIntoQueue(any(TransactionWithTime.class)); // store both transactions
    }

    @Test
    void processQxTransactions_givenNoEvents_thenCallOrderBookProcessor() {
        Transaction transaction = testTransaction();

        Trade trade = mock();
        when(orderBookProcessor.calculateTrades(42, Instant.EPOCH, Set.of(new Asset("issuer", "asset")), List.of(transaction))).thenReturn(Mono.just(List.of(trade)));

        StepVerifier.create(transactionProcessor.processQxTransactions(42, Instant.EPOCH, List.of(), List.of(transaction)).log())
                .expectNext(List.of(trade))
                .verifyComplete();
    }

    private Transaction testTransaction() {
        OrderType orderType = OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        return new Transaction("hash", "sourceId", "destinationId", 123, 42, orderType.code, 0, orderData, null);
    }

}