package org.qubic.qx.api.controller.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.db.TransactionsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        Pageable pageable = PageRequest.of(0, 50);
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByInputTypesOrdered(TRANSFER_INPUT_TYPE, 0, 50)).thenReturn(expected);
        assertThat(transactionsService.getTransferTransactions(pageable)).isEqualTo(expected);
    }

    @Test
    void getTransferTransactionsForAsset() {
        Pageable pageable = PageRequest.of(0, 50);
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByAssetOrdered("issuer", "asset", TRANSFER_INPUT_TYPE, 0, 50)).thenReturn(expected);
        assertThat(transactionsService.getTransferTransactionsForAsset("issuer", "asset", pageable)).isEqualTo(expected);
    }

    @Test
    void getTransferTransactionsForEntity() {
        Pageable pageable = PageRequest.of(0, 50);
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findTransfersByEntityOrdered("identity", 0, 50)).thenReturn(expected);
        assertThat(transactionsService.getTransferTransactionsForEntity("identity", pageable)).isEqualTo(expected);
    }

    // issued assets

    @Test
    void getIssuedAssets() {
        Pageable pageable = PageRequest.of(0, 50);
        List<TransactionDto> expected = List.of(mock(), mock());
        when(transactionsRepository.findByInputTypesOrdered(List.of(1), 0, 50))
                .thenReturn(expected);
        assertThat(transactionsService.getIssuedAssets(pageable)).isEqualTo(expected);
    }

}
