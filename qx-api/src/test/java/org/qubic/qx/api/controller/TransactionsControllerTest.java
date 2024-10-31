package org.qubic.qx.api.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.db.domain.ExtraData;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.QxIssueAssetData;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionsControllerTest {

    private final TransactionsService service = mock();
    private final TransactionsController controller = new TransactionsController(service);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getTransferTransactions() {
        TransactionDto transaction = transaction(2);
        when(service.getTransferTransactions()).thenReturn(List.of(transaction, transaction));
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
        when(service.getTransferTransactionsForAsset("ISSUER", "ASSET"))
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
        when(service.getTransferTransactionsForEntity("IDENTITY")).thenReturn(List.of(transaction, transaction));
        client.get().uri("/entity/IDENTITY/transfers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    @Test
    void getOrderTransactions() {
        TransactionDto transaction = transaction(5);
        when(service.getOrderTransactions()).thenReturn(List.of(transaction, transaction));
        client.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    @Test
    void getOrderTransactionsForAsset() {
        TransactionDto transaction = transaction(6);
        when(service.getOrderTransactionsForAsset("ISSUER", "ASSET"))
                .thenReturn(List.of(transaction, transaction));
        client.get().uri("/issuer/ISSUER/asset/ASSET/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    @Test
    void getOrderTransactionsForEntity() {
        TransactionDto transaction = transaction(7);
        when(service.getOrderTransactionsForEntity("IDENTITY"))
                .thenReturn(List.of(transaction, transaction));
        client.get().uri("/entity/IDENTITY/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionDto.class)
                .hasSize(2)
                .contains(transaction, transaction);
    }

    @Test
    void getIssuedAssets() {
        TransactionDto transaction = transaction(1);
        when(service.getIssuedAssets()).thenReturn(List.of(transaction));
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
                    RandomStringUtils.random(7),
                    (long) (Math.random() * 10000),
                    "01010100",
                    (byte) 20
            );
        } else if (inputType == 2) {
            extraData = new QxTransferAssetData(
              RandomStringUtils.random(32),
              RandomStringUtils.random(7),
              RandomStringUtils.random(32),
              42
            );
        } else {
            extraData = new QxAssetOrderData(
              RandomStringUtils.random(32),
              RandomStringUtils.random(32),
              123,
                    42
            );
        }
        return new TransactionDto(
                RandomStringUtils.random(32),
                RandomStringUtils.random(32),
                42,
                123,
                inputType,
                extraData,
                true
        );
    }

}