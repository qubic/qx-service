package org.qubic.qx.sync.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.Qx.OrderType;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import org.qubic.qx.sync.repository.domain.TransactionMessage;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionProcessorTest {

    private final TransactionRepository transactionRepository = mock();
    private final TradeRepository tradeRepository = mock();
    private final EventsProcessor eventsProcessor = mock();
    private final TransactionProcessor transactionProcessor = new TransactionProcessor(transactionRepository, tradeRepository, eventsProcessor);


    @BeforeEach
    void initMocks() {
        when(tradeRepository.putTradeIntoQueue(any(Trade.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactionRepository.putTransactionIntoQueue(any(TransactionMessage.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void processTransaction_givenTrade_thenStoreTransaction() {
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        TransactionWithMeta transaction = testTransaction(te);
        // transaction is only stored if there is a trade
        Trade trade = mock();
        when(eventsProcessor.calculateTrades(eq(transaction), any())).thenReturn(List.of(trade));

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .expectNextCount(1)
                .verifyComplete();
        verify(transactionRepository).putTransactionIntoQueue(any(TransactionMessage.class));
    }

    @Test
    void processTransaction_givenNoTrade_thenNotifyOnly() {
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        TransactionWithMeta transaction = testTransaction(te);
        // transaction is only stored if there is a trade
        when(eventsProcessor.calculateTrades(eq(transaction), any())).thenReturn(List.of());

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .assertNext(message -> assertTransactionMessage(message, false))
                .verifyComplete();

        verify(transactionRepository).putTransactionIntoQueue(any(TransactionMessage.class));
    }

    @Test
    void processTransaction_givenTrade_thenStoreTrade() {
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        TransactionWithMeta transaction = testTransaction(te);
        Trade trade = mock();
        when(eventsProcessor.calculateTrades(eq(transaction), any())).thenReturn(List.of(trade));

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .expectNextCount(1)
                .verifyComplete();

        verify(tradeRepository).putTradeIntoQueue(trade);
    }

    @Test
    void processTransaction_givenNoTrade_thenDoNotStoreTrade() {
        TransactionWithMeta transaction = testTransaction();
        when(eventsProcessor.calculateTrades(eq(transaction), any())).thenReturn(List.of());

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .assertNext(message -> assertTransactionMessage(message, false))
                .verifyComplete();

        verifyNoInteractions(tradeRepository);
    }

    @Test
    void processTransaction_givenTransfer_thenStoreTransaction() {
        QxTransferAssetData extraData = new QxTransferAssetData("issuer", "name", "new-owner-id", 42);
        TransactionWithMeta transaction = testTransaction(extraData, OrderType.TRANSFER_SHARE);
        when(eventsProcessor.isAssetTransferred(any())).thenReturn(true);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .assertNext(message -> assertTransactionMessage(message, true))
                .verifyComplete();

        verify(transactionRepository).putTransactionIntoQueue(any(TransactionMessage.class));

    }

    @Test
    void processTransaction_givenNoTransfer_thenDoNotStoreTransaction() {
        QxTransferAssetData extraData = new QxTransferAssetData("issuer", "name", "new-owner-id", 42);
        TransactionWithMeta transaction = testTransaction(extraData, OrderType.TRANSFER_SHARE);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .verifyComplete();

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void processTransaction_givenIssuance_thenDoStoreTransaction() {
        QxIssueAssetData extraData = new QxIssueAssetData("name", 123456, "0000000", (byte) 0);
        TransactionWithMeta transaction = testTransaction(extraData, OrderType.ISSUE_ASSET);
        when(eventsProcessor.isAssetIssued(any())).thenReturn(true);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .assertNext(message -> assertTransactionMessage(message, true))
                .verifyComplete();

        verify(transactionRepository).putTransactionIntoQueue(any(TransactionMessage.class));
    }

    @Test
    void processTransaction_givenNoIssuance_thenDoStoreTransaction() {
        QxIssueAssetData extraData = new QxIssueAssetData("name", 123456, "0000000", (byte) 0);
        TransactionWithMeta transaction = testTransaction(extraData, OrderType.ISSUE_ASSET);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction))
                .verifyComplete();

        verifyNoInteractions(transactionRepository);
    }

    private static void assertTransactionMessage(Object message, boolean relevantEvents) {
        assertThat(message).isInstanceOf(TransactionMessage.class);
        assertThat(((TransactionMessage) message).relevantEvents()).isEqualTo(relevantEvents);
    }


    private TransactionWithMeta testTransaction(TransactionEvent... events) {
        OrderType orderType = OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        return testTransaction(orderData, orderType, events);
    }

    private TransactionWithMeta testTransaction(ExtraData extraData, OrderType orderType, TransactionEvent... events) {
        Transaction transaction = new Transaction("transaction-hash",
                "source-public-id",
                "destination-public-id",
                123,
                42,
                orderType.code,
                0,
                extraData);
        return TransactionWithMeta.builder()
                .transaction(transaction)
                .events(List.of(events))
                .time(Instant.EPOCH)
                .build();
    }

}