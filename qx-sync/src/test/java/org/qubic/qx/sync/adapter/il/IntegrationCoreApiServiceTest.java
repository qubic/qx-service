package org.qubic.qx.sync.adapter.il;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.il.domain.IlTransaction;
import org.qubic.qx.sync.adapter.il.domain.IlTransactions;
import org.qubic.qx.sync.adapter.il.mapping.IlCoreMapper;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationCoreApiServiceTest {

    private final WebClient webClient = mock();
    private final IlCoreMapper mapper = mock();
    private final IntegrationCoreApiService service = spy(new IntegrationCoreApiService(webClient, mapper, 3));

    @Test
    void getQxTransactions_shouldFilterRelevantTransactions() {
        long tick = 12345L;
        
        // Relevant transaction (sent to QX address with a valid input type)
        createTransaction(tick, 5, "txId1");
        IlTransaction relevantTransaction = createTransaction(tick, 5, "txId1"); //

        // should be filtered
        IlTransaction wrongDestination = new IlTransaction(
                "SOURCE_ID",
                "DIFFERENT_ADDRESS", // wrong destination
                "1000",
                tick,
                5,
                56,
                "base64input",
                "txId2"
        );
        
        // should be filtered
        IlTransaction wrongInputType = new IlTransaction(
                "SOURCE_ID",
                Qx.QX_PUBLIC_ID,
                "1000",
                tick,
                99, // invalid input type
                556,
                "base64input",
                "txId3"
        );
        
        IlTransactions ilTransactions = new IlTransactions(List.of(relevantTransaction, wrongDestination, wrongInputType));
        Transaction mappedTransaction = mock(Transaction.class);
        doReturn(Mono.just(ilTransactions)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(relevantTransaction)).thenReturn(mappedTransaction);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mappedTransaction)
                .verifyComplete();
        
        // Verify only the relevant transaction was mapped
        verify(mapper, times(1)).mapTransaction(relevantTransaction);
        verify(mapper, never()).mapTransaction(wrongDestination);
        verify(mapper, never()).mapTransaction(wrongInputType);
    }

    @Test
    void getQxTransactions_shouldHandleAllValidInputTypes() {
        long tick = 12345L;
        
        // Create transactions with all valid input types
        IlTransaction issueAsset = createTransaction(tick, 1, "txId1"); // ISSUE_ASSET
        IlTransaction transferShare = createTransaction(tick, 2, "txId2"); // TRANSFER_SHARE
        IlTransaction addAsk = createTransaction(tick, 5, "txId3"); // ADD_ASK
        IlTransaction addBid = createTransaction(tick, 6, "txId4"); // ADD_BID
        IlTransaction removeAsk = createTransaction(tick, 7, "txId5"); // REMOVE_ASK
        IlTransaction removeBid = createTransaction(tick, 8, "txId6"); // REMOVE_BID
        
        IlTransactions ilTransactions = new IlTransactions(
                List.of(issueAsset, transferShare, addAsk, addBid, removeAsk, removeBid)
        );
        
        Transaction mockTx = mock(Transaction.class);
        
        doReturn(Mono.just(ilTransactions)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(any(IlTransaction.class))).thenReturn(mockTx);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .verifyComplete();
        
        // Verify all were mapped
        verify(mapper, times(6)).mapTransaction(any(IlTransaction.class));
    }

    @Test
    void getQxTransactions_whenMapTransactionFails_shouldSkipFailedTransaction() {
        // Given
        long tick = 12345L;
        
        IlTransaction successfulTransaction = createTransaction(tick, 5, "txId1");
        IlTransaction failingTransaction = createTransaction(tick, 6, "txId2");
        IlTransaction anotherSuccessfulTransaction = createTransaction(tick, 7, "txId3");
        
        IlTransactions ilTransactions = new IlTransactions(
                List.of(successfulTransaction, failingTransaction, anotherSuccessfulTransaction)
        );
        
        Transaction mappedTx1 = mock(Transaction.class);
        Transaction mappedTx2 = mock(Transaction.class);
        
        doReturn(Mono.just(ilTransactions)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(successfulTransaction)).thenReturn(mappedTx1);
        when(mapper.mapTransaction(failingTransaction)).thenThrow(new RuntimeException("Mapping failed"));
        when(mapper.mapTransaction(anotherSuccessfulTransaction)).thenReturn(mappedTx2);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mappedTx1)
                .expectNext(mappedTx2)
                .verifyComplete();
        
        // Verify all transactions were attempted to be mapped
        verify(mapper, times(1)).mapTransaction(successfulTransaction);
        verify(mapper, times(1)).mapTransaction(failingTransaction);
        verify(mapper, times(1)).mapTransaction(anotherSuccessfulTransaction);
    }

    @Test
    void getQxTransactions_whenAllTransactionsFail_shouldReturnEmpty() {
        long tick = 12345L;
        
        IlTransaction failingTransaction1 = createTransaction(tick, 5, "txId1");
        IlTransaction failingTransaction2 = createTransaction(tick, 6, "txId2");
        
        IlTransactions ilTransactions = new IlTransactions(
                List.of(failingTransaction1, failingTransaction2)
        );
        
        doReturn(Mono.just(ilTransactions)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(any(IlTransaction.class))).thenThrow(new RuntimeException("Mapping failed"));
        
        StepVerifier.create(service.getQxTransactions(tick))
                .verifyComplete();
        
        // Verify all transactions were attempted to be mapped
        verify(mapper, times(2)).mapTransaction(any(IlTransaction.class));
    }

    @Test
    void getQxTransactions_withEmptyTransactionList_shouldReturnEmpty() {
        long tick = 12345L;
        IlTransactions emptyTransactions = new IlTransactions(List.of());
        
        doReturn(Mono.just(emptyTransactions)).when(service).getTickTransactions(tick);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .verifyComplete();
        
        verify(mapper, never()).mapTransaction(any(IlTransaction.class));
    }

    @Test
    void getQxTransactions_withMixedRelevantAndIrrelevantTransactions_shouldFilterAndMap() {
        long tick = 12345L;
        
        IlTransaction relevant1 = createTransaction(tick, 5, "txId1");
        IlTransaction irrelevant1 = new IlTransaction("SOURCE", "WRONG_DEST", "100", tick, 5, 48, "input", "txId2");
        IlTransaction relevant2 = createTransaction(tick, 6, "txId3");
        IlTransaction irrelevant2 = new IlTransaction("SOURCE", Qx.QX_PUBLIC_ID, "100", tick, 99, 48, "input", "txId4");
        IlTransaction relevant3 = createTransaction(tick, 7, "txId5");
        
        IlTransactions ilTransactions = new IlTransactions(
                List.of(relevant1, irrelevant1, relevant2, irrelevant2, relevant3)
        );
        
        Transaction mappedTx1 = mock(Transaction.class);
        Transaction mappedTx2 = mock(Transaction.class);
        Transaction mappedTx3 = mock(Transaction.class);
        
        doReturn(Mono.just(ilTransactions)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(relevant1)).thenReturn(mappedTx1);
        when(mapper.mapTransaction(relevant2)).thenReturn(mappedTx2);
        when(mapper.mapTransaction(relevant3)).thenReturn(mappedTx3);
        
        // When & Then
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mappedTx1)
                .expectNext(mappedTx2)
                .expectNext(mappedTx3)
                .verifyComplete();
        
        // Verify only relevant transactions were mapped
        verify(mapper, times(1)).mapTransaction(relevant1);
        verify(mapper, times(1)).mapTransaction(relevant2);
        verify(mapper, times(1)).mapTransaction(relevant3);
        verify(mapper, never()).mapTransaction(irrelevant1);
        verify(mapper, never()).mapTransaction(irrelevant2);
    }

    private IlTransaction createTransaction(long tick, int inputType, String txId) {
        return new IlTransaction(
                "SOURCE_ID",
                Qx.QX_PUBLIC_ID,
                "1000",
                tick,
                inputType,
                48,
                "base64input",
                txId
        );
    }
}
