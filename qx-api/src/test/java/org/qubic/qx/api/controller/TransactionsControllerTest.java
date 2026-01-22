package org.qubic.qx.api.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.db.domain.ExtraData;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.QxIssueAssetData;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionsControllerTest {

    private final TransactionsService service = mock();
    private final TransactionsController controller = new TransactionsController(service);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .argumentResolvers(enablePaging())
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getTransferTransactions() {
        TransactionDto transaction = transaction(2);
        when(service.getTransferTransactions(any(Pageable.class))).thenReturn(List.of(transaction, transaction));
        client.get().uri("/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    @Test
    void getTransferTransactionsForAsset() {
        TransactionDto transaction = transaction(2);
        when(service.getTransferTransactionsForAsset(eq("ISSUER"), eq("ASSET"), any(Pageable.class)))
                .thenReturn(List.of(transaction, transaction));
        client.get().uri("/issuer/ISSUER/asset/ASSET/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    @Test
    void getTransferTransactionsForEntity() {
        TransactionDto transaction = transaction(2);
        when(service.getTransferTransactionsForEntity(eq("IDENTITY"), any(Pageable.class))).thenReturn(List.of(transaction, transaction));
        client.get().uri("/entity/IDENTITY/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    private static Consumer<ArgumentResolverConfigurer> enablePaging() {
        return resolvers -> resolvers.addCustomResolver(new ReactivePageableHandlerMethodArgumentResolver());
    }

    @Test
    void getIssuedAssets() {
        TransactionDto transaction = transaction(1);
        when(service.getIssuedAssets(any(Pageable.class))).thenReturn(List.of(transaction));
        client.get().uri("/issued-assets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(1)
                .contains(transaction);
    }

    private TransactionDto transaction(int inputType) {
        ExtraData extraData;
        if (inputType == 1) {
            extraData = new QxIssueAssetData(
                    RandomStringUtils.insecure().nextAlphabetic(7),
                    (long) (Math.random() * 10000),
                    "01010100",
                    (byte) 20
            );
        } else if (inputType == 2) {
            extraData = new QxTransferAssetData(
              RandomStringUtils.insecure().nextAlphabetic(32),
              RandomStringUtils.insecure().nextAlphabetic(7),
              RandomStringUtils.insecure().nextAlphabetic(32),
              42
            );
        } else {
            extraData = new QxAssetOrderData(
              RandomStringUtils.insecure().nextAlphabetic(32),
              RandomStringUtils.insecure().nextAlphabetic(32),
              123,
                    42
            );
        }
        return new TransactionDto(
                Instant.now(),
                RandomStringUtils.insecure().nextAlphabetic(32),
                RandomStringUtils.insecure().nextAlphabetic(32),
                42,
                123,
                inputType,
                extraData,
                true
        );
    }

}
