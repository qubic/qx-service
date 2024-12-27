package org.qubic.qx.sync.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.Qx.OrderType;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.Trade;
import org.qubic.qx.sync.domain.TransactionEvent;
import org.qubic.qx.sync.domain.TransactionWithTime;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

class TransactionProcessorTest {

    private final TransactionRepository transactionRepository = mock();
    private final TradeRepository tradeRepository = mock();
    private final EventsProcessor eventsProcessor = mock();
    private final TransactionProcessor transactionProcessor = new TransactionProcessor(transactionRepository, tradeRepository, eventsProcessor);


    @BeforeEach
    void initMocks() {
        when(tradeRepository.putTradeIntoQueue(any(Trade.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactionRepository.putTransactionIntoQueue(any(TransactionWithTime.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void processQxTransactions_thenPutTransactionIntoQueue() {
        TransactionWithTime transaction = testTransaction();
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        Trade trade = mock();
        when(eventsProcessor.calculateTrades(eq(transaction), anyList(), any())).thenReturn(List.of(trade));

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of(te)))
                .expectNextCount(1)
                .verifyComplete();
        verify(transactionRepository).putTransactionIntoQueue(any(TransactionWithTime.class));
    }

    @Test
    void processQxTransactions_thenStoreTrades() {
        TransactionWithTime transaction = testTransaction();
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        Trade trade = mock();
        when(eventsProcessor.calculateTrades(eq(transaction), anyList(), any())).thenReturn(List.of(trade));

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of(te)))
                .expectNextCount(1)
                .verifyComplete();

        verify(tradeRepository).putTradeIntoQueue(trade);
    }

    @Test
    void processTransactions_givenOrderTransactionWithoutTrade_thenDoNotStore() {
        TransactionWithTime transaction = testTransaction();
        when(eventsProcessor.calculateTrades(eq(transaction), anyList(), any())).thenReturn(List.of());

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of()))
                .expectNextCount(1)
                .verifyComplete();

        verifyNoInteractions(tradeRepository);
        verifyNoInteractions(transactionRepository);
    }

    private TransactionWithTime testTransaction() {
        OrderType orderType = OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        return new TransactionWithTime("hash", "sourceId", "destinationId", 123, 42, Instant.EPOCH.getEpochSecond(), orderType.code, 0, orderData, null);
    }

}