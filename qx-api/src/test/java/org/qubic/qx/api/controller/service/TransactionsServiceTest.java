package org.qubic.qx.api.controller.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.dto.TransactionDto;
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
        when(transactionsRepository.findByInputTypesOrdered(TRANSFER_INPUT_TYPE, 50)).thenReturn(expected);
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

    // issued assets

    @Test
    void getIssuedAssets() {
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByInputTypesOrdered(List.of(1), LIMIT))
                .thenReturn(expected);
        assertThat(transactionsService.getIssuedAssets()).isEqualTo(expected);
    }

}