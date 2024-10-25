package org.qubic.qx.api.controller.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.db.TransactionsRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.qubic.qx.api.controller.service.TransactionsService.*;

class TransactionsServiceTest {

    private final TransactionsRepository transactionsRepository = mock(TransactionsRepository.class);
    private final TransactionsService transactionsService = new TransactionsService(transactionsRepository);

    // transfers

    @Test
    void getTransferTransactions() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByInputTypesOrdered(TRANSFER_INPUT_TYPE, LIMIT)).thenReturn(expected);
        assertThat(transactionsService.getTransferTransactions()).isEqualTo(expected);
    }

    @Test
    void getTransferTransactionsForAsset() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByAssetOrdered("issuer", "asset", TRANSFER_INPUT_TYPE, LIMIT)).thenReturn(expected);
        assertThat(transactionsService.getTransferTransactionsForAsset("issuer", "asset")).isEqualTo(expected);
    }

    @Test
    void getTransferTransactionsForEntity() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findTransfersByEntityOrdered("identity", LIMIT)).thenReturn(expected);
        assertThat(transactionsService.getTransferTransactionsForEntity("identity")).isEqualTo(expected);
    }

    // orders

    @Test
    void getOrderTransactions() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByInputTypesOrdered(ORDER_INPUT_TYPES, LIMIT)).thenReturn(expected);
        assertThat(transactionsService.getOrderTransactions()).isEqualTo(expected);
    }

    @Test
    void getOrderTransactionsForAsset() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByAssetOrdered("issuer", "asset", ORDER_INPUT_TYPES, LIMIT)).thenReturn(expected);
        assertThat(transactionsService.getOrderTransactionsForAsset("issuer", "asset")).isEqualTo(expected);
    }

    @Test
    void getOrderTransactionsForEntity() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findBySourceEntityOrdered("identity", ORDER_INPUT_TYPES, LIMIT)).thenReturn(expected);
        assertThat(transactionsService.getOrderTransactionsForEntity("identity")).isEqualTo(expected);
    }

}