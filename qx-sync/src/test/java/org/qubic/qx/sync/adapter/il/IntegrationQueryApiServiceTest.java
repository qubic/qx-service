package org.qubic.qx.sync.adapter.il;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiLastProcessedTick;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTickData;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTickDataResponse;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTransaction;
import org.qubic.qx.sync.adapter.il.mapping.IlQueryApiMapper;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.Mockito.*;

class IntegrationQueryApiServiceTest {

    private final WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
    private final IlQueryApiMapper mapper = mock();
    private final IntegrationQueryApiService service = spy(new IntegrationQueryApiService(webClient, mapper, 3));

    @Test
    void getQxTransactions_shouldFilterRelevantTransactions() {
        long tick = 12345L;
        
        // Relevant transaction (sent to QX address with a valid input type)
        IlQueryApiTransaction relevantTransaction = createTransaction(tick, 5, "txId1");

        // should be filtered - invalid input type
        IlQueryApiTransaction wrongInputType = createTransaction(tick, 99, "txId3");
        
        Transaction mappedTransaction = mock(Transaction.class);
        doReturn(Flux.just(relevantTransaction, wrongInputType)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(relevantTransaction)).thenReturn(mappedTransaction);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mappedTransaction)
                .verifyComplete();
        
        // Verify only the relevant transaction was mapped
        verify(mapper, times(1)).mapTransaction(relevantTransaction);
        verify(mapper, never()).mapTransaction(wrongInputType);
    }

    @Test
    void getQxTransactions_shouldHandleAllValidInputTypes() {
        long tick = 12345L;
        
        // Create transactions with all valid input types
        IlQueryApiTransaction issueAsset = createTransaction(tick, 1, "txId1"); // ISSUE_ASSET
        IlQueryApiTransaction transferShare = createTransaction(tick, 2, "txId2"); // TRANSFER_SHARE
        IlQueryApiTransaction addAsk = createTransaction(tick, 5, "txId3"); // ADD_ASK
        IlQueryApiTransaction addBid = createTransaction(tick, 6, "txId4"); // ADD_BID
        IlQueryApiTransaction removeAsk = createTransaction(tick, 7, "txId5"); // REMOVE_ASK
        IlQueryApiTransaction removeBid = createTransaction(tick, 8, "txId6"); // REMOVE_BID
        
        Transaction mockTx = mock(Transaction.class);
        
        doReturn(Flux.just(issueAsset, transferShare, addAsk, addBid, removeAsk, removeBid))
                .when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(any(IlQueryApiTransaction.class))).thenReturn(mockTx);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .expectNext(mockTx)
                .verifyComplete();
        
        // Verify all were mapped
        verify(mapper, times(6)).mapTransaction(any(IlQueryApiTransaction.class));
    }

    @Test
    void getQxTransactions_whenMapTransactionFails_shouldSkipFailedTransaction() {
        // Given
        long tick = 12345L;
        
        IlQueryApiTransaction transaction1 = createTransaction(tick, 5, "txId1");
        IlQueryApiTransaction transaction2 = createTransaction(tick, 6, "txId2");
        IlQueryApiTransaction transaction3 = createTransaction(tick, 7, "txId3");
        
        Transaction mappedTx1 = mock(Transaction.class);
        Transaction mappedTx3 = mock(Transaction.class);
        
        doReturn(Flux.just(transaction1, transaction2, transaction3)).when(service).getTickTransactions(tick);
        
        // transaction2 mapping fails
        when(mapper.mapTransaction(transaction1)).thenReturn(mappedTx1);
        when(mapper.mapTransaction(transaction2)).thenThrow(new RuntimeException("Mapping failed"));
        when(mapper.mapTransaction(transaction3)).thenReturn(mappedTx3);
        
        // When & Then
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mappedTx1)
                .expectNext(mappedTx3)
                .verifyComplete();
        
        verify(mapper, times(3)).mapTransaction(any(IlQueryApiTransaction.class));
    }

    @Test
    void getQxTransactions_whenAllTransactionsFail_shouldReturnEmpty() {
        long tick = 12345L;
        
        IlQueryApiTransaction transaction1 = createTransaction(tick, 5, "txId1");
        IlQueryApiTransaction transaction2 = createTransaction(tick, 6, "txId2");
        
        doReturn(Flux.just(transaction1, transaction2)).when(service).getTickTransactions(tick);
        when(mapper.mapTransaction(any(IlQueryApiTransaction.class)))
                .thenThrow(new RuntimeException("Mapping failed"));
        
        StepVerifier.create(service.getQxTransactions(tick))
                .verifyComplete();
        
        verify(mapper, times(2)).mapTransaction(any(IlQueryApiTransaction.class));
    }

    @Test
    void getQxTransactions_withEmptyTransactionList_shouldReturnEmpty() {
        long tick = 12345L;
        
        doReturn(Flux.empty()).when(service).getTickTransactions(tick);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .verifyComplete();
        
        verify(mapper, never()).mapTransaction(any(IlQueryApiTransaction.class));
    }

    @Test
    void getQxTransactions_withMixedRelevantAndIrrelevantTransactions_shouldFilterAndMap() {
        long tick = 12345L;
        
        IlQueryApiTransaction relevant1 = createTransaction(tick, 1, "txId1");
        IlQueryApiTransaction relevant2 = createTransaction(tick, 5, "txId2");
        IlQueryApiTransaction wrongInputType = createTransaction(tick, 999, "txId4");
        IlQueryApiTransaction relevant3 = createTransaction(tick, 8, "txId5");

        Transaction mappedTx1 = mock(Transaction.class);
        Transaction mappedTx2 = mock(Transaction.class);
        Transaction mappedTx3 = mock(Transaction.class);
        
        doReturn(Flux.just(relevant1, relevant2, wrongInputType, relevant3))
                .when(service).getTickTransactions(tick);
        
        when(mapper.mapTransaction(relevant1)).thenReturn(mappedTx1);
        when(mapper.mapTransaction(relevant2)).thenReturn(mappedTx2);
        when(mapper.mapTransaction(relevant3)).thenReturn(mappedTx3);
        
        StepVerifier.create(service.getQxTransactions(tick))
                .expectNext(mappedTx1)
                .expectNext(mappedTx2)
                .expectNext(mappedTx3)
                .verifyComplete();
        
        // Verify only relevant transactions were mapped
        verify(mapper, times(1)).mapTransaction(relevant1);
        verify(mapper, times(1)).mapTransaction(relevant2);
        verify(mapper, times(1)).mapTransaction(relevant3);
        verify(mapper, never()).mapTransaction(wrongInputType);
    }

    @Test
    void getTickInfo_happyPath() {
        IlQueryApiLastProcessedTick apiDto = new IlQueryApiLastProcessedTick(300L, 200, 100L);
        TickInfo mapped = new TickInfo(200, 300L, 100L);

        when(webClient.get()
                .uri("/query/v1/getLastProcessedTick")
                .retrieve()
                .bodyToMono(IlQueryApiLastProcessedTick.class))
                .thenReturn(Mono.just(apiDto));
        when(mapper.map(apiDto)).thenReturn(mapped);

        StepVerifier.create(service.getTickInfo())
                .expectNext(mapped)
                .verifyComplete();
    }

    @Test
    void getCurrentTick_happyPath() {
        IlQueryApiLastProcessedTick apiDto = new IlQueryApiLastProcessedTick(300L, 200, 100L);
        TickInfo mapped = new TickInfo(200, 300L, 100L);

        when(webClient.get()
                .uri("/query/v1/getLastProcessedTick")
                .retrieve()
                .bodyToMono(IlQueryApiLastProcessedTick.class))
                .thenReturn(Mono.just(apiDto));
        when(mapper.map(apiDto)).thenReturn(mapped);

        StepVerifier.create(service.getCurrentTick())
                .expectNext(300L)
                .verifyComplete();
    }

    @Test
    void getTickData_happyPath() {
        long tickNumber = 44191622L;

        IlQueryApiTickData apiTickData = new IlQueryApiTickData(tickNumber, 200, 1_771_268_260_000L);
        IlQueryApiTickDataResponse apiResponse = new IlQueryApiTickDataResponse(apiTickData);
        TickData mapped = new TickData(200, tickNumber, Instant.ofEpochMilli(1_771_268_260_000L));

        when(webClient.post()
                .uri("/query/v1/getTickData")
                .bodyValue(any())
                .retrieve()
                .bodyToMono(IlQueryApiTickDataResponse.class))
                .thenReturn(Mono.just(apiResponse));
        when(mapper.map(apiTickData)).thenReturn(mapped);

        StepVerifier.create(service.getTickData(tickNumber))
                .expectNext(mapped)
                .verifyComplete();
    }

    @Test
    void getTickData_whenTickDataIsNull_shouldReturnEmptyTickData() {
        long tickNumber = 44191622L;

        IlQueryApiTickDataResponse apiResponse = new IlQueryApiTickDataResponse(null);
        IlQueryApiTickData emptyApiTickData = new IlQueryApiTickData(0, 0, 0);
        TickData mapped = new TickData(0, 0, Instant.ofEpochMilli(0));

        when(webClient.post()
                .uri("/query/v1/getTickData")
                .bodyValue(any())
                .retrieve()
                .bodyToMono(IlQueryApiTickDataResponse.class))
                .thenReturn(Mono.just(apiResponse));
        
        when(mapper.map(emptyApiTickData)).thenReturn(mapped);

        StepVerifier.create(service.getTickData(tickNumber))
                .expectNext(mapped)
                .verifyComplete();
        
        verify(mapper).map(emptyApiTickData);
    }

    @Test
    void getTickTransactions_whenApiReturnsEmpty_shouldReturnEmptyFlux() {
        long tick = 12345L;

        when(webClient.post()
                .uri("/query/v1/getTransactionsForTick")
                .bodyValue(any())
                .retrieve()
                .bodyToFlux(IlQueryApiTransaction.class))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getTickTransactions(tick))
                .verifyComplete();
    }

    @Test
    void getQxTransactions_whenApiReturnsEmpty_shouldReturnEmptyFlux() {
        long tick = 12345L;

        // Ensure we're testing the real `getTickTransactions` call (not spy-stubbed)
        // by making sure `getTickTransactions` is not stubbed in this test.

        when(webClient.post()
                .uri("/query/v1/getTransactionsForTick")
                .bodyValue(any())
                .retrieve()
                .bodyToFlux(IlQueryApiTransaction.class))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getQxTransactions(tick))
                .verifyComplete();

        verify(mapper, never()).mapTransaction(any());
    }

    private IlQueryApiTransaction createTransaction(long tick, int inputType, String txId) {
        return new IlQueryApiTransaction(
                txId,
                1000L,
                "SOURCE_ID",
                Qx.QX_PUBLIC_ID,
                tick,
                inputType,
                56,
                "base64input"
        );
    }
}
