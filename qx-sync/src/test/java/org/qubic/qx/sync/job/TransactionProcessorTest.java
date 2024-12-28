package org.qubic.qx.sync.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.Qx.OrderType;
import org.qubic.qx.sync.domain.*;
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
    void processTransaction_givenTrade_thenStoreTransaction() {
        TransactionWithTime transaction = testTransaction();
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        // transaction is only stored if there is a trade
        Trade trade = mock();
        when(eventsProcessor.calculateTrades(eq(transaction), anyList(), any())).thenReturn(List.of(trade));

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of(te)))
                .expectNextCount(1)
                .verifyComplete();
        verify(transactionRepository).putTransactionIntoQueue(any(TransactionWithTime.class));
    }

    @Test
    void processTransaction_givenNoTrade_thenClearCacheOnly() {
        TransactionWithTime transaction = testTransaction();
        TransactionEvent te = new TransactionEvent(null, 1, 2, "foo");
        // transaction is only stored if there is a trade
        when(eventsProcessor.calculateTrades(eq(transaction), anyList(), any())).thenReturn(List.of());

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of(te)))
                .expectNextCount(1)
                .verifyComplete();

        // TODO verify that transaction with info that is wasn't successful was sent

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void processTransaction_givenTrade_thenStoreTrade() {
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
    void processTransaction_givenNoTrade_thenDoNotStoreTrade() {
        TransactionWithTime transaction = testTransaction();
        when(eventsProcessor.calculateTrades(eq(transaction), anyList(), any())).thenReturn(List.of());

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of()))
                .expectNextCount(1)
                .verifyComplete();

        verifyNoInteractions(tradeRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void processTransaction_givenTransfer_thenStoreTransaction() {
        QxTransferAssetData extraData = new QxTransferAssetData("issuer", "name", "new-owner-id", 42);
        TransactionWithTime transaction = testTransaction(extraData, OrderType.TRANSFER_SHARE);
        when(eventsProcessor.isAssetTransferred(any())).thenReturn(true);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of()))
                .expectNextCount(1)
                .verifyComplete();

        verify(transactionRepository).putTransactionIntoQueue(transaction);

    }

    @Test
    void processTransaction_givenNoTransfer_thenDoNotStoreTransaction() {
        QxTransferAssetData extraData = new QxTransferAssetData("issuer", "name", "new-owner-id", 42);
        TransactionWithTime transaction = testTransaction(extraData, OrderType.TRANSFER_SHARE);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of()))
                .expectNextCount(1)
                .verifyComplete();

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void processTransaction_givenIssuance_thenDoStoreTransaction() {
        QxIssueAssetData extraData = new QxIssueAssetData("name", 123456, "0000000", (byte) 0);
        TransactionWithTime transaction = testTransaction(extraData, OrderType.ISSUE_ASSET);
        when(eventsProcessor.isAssetIssued(any())).thenReturn(true);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of()))
                .expectNextCount(1)
                .verifyComplete();

        verify(transactionRepository).putTransactionIntoQueue(transaction);
    }

    @Test
    void processTransaction_givenNoIssuance_thenDoStoreTransaction() {
        QxIssueAssetData extraData = new QxIssueAssetData("name", 123456, "0000000", (byte) 0);
        TransactionWithTime transaction = testTransaction(extraData, OrderType.ISSUE_ASSET);

        StepVerifier.create(transactionProcessor.processQxTransaction(transaction, List.of()))
                .expectNextCount(1)
                .verifyComplete();

        verifyNoInteractions(transactionRepository);
    }


    private TransactionWithTime testTransaction() {
        OrderType orderType = OrderType.ADD_BID;
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 5, 5);
        return testTransaction(orderData, orderType);
    }

    private TransactionWithTime testTransaction(ExtraData extraData, OrderType orderType) {
        return new TransactionWithTime("transaction-hash",
                "source-public-id",
                "destination-public-id",
                123,
                42,
                Instant.EPOCH.getEpochSecond(),
                orderType.code,
                0,
                extraData,
                null);
    }

}