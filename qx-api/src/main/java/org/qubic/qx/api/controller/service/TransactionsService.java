package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.db.TransactionsRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class TransactionsService {

    static final int LIMIT = 100;
    static final List<Integer> TRANSFER_INPUT_TYPE = List.of(2);
    static final List<Integer> ISSUE_ASSET_INPUT_TYPE = List.of(1);

    private final TransactionsRepository transactionsRepository;

    public TransactionsService(TransactionsRepository transactionsRepository) {
        this.transactionsRepository = transactionsRepository;
    }

    // transfers

    public List<TransactionDto> getTransferTransactions(Pageable pageable) {
        return transactionsRepository.findByInputTypesOrdered(TRANSFER_INPUT_TYPE, pageable.getOffset(), pageable.getPageSize());
    }

    public List<TransactionDto> getTransferTransactionsForAsset(String issuer, String asset, Pageable pageable) {
        return transactionsRepository.findByAssetOrdered(issuer, asset, TRANSFER_INPUT_TYPE, pageable.getOffset(), pageable.getPageSize());
    }

    public List<TransactionDto> getTransferTransactionsForEntity(String identity, Pageable pageable) {
        return transactionsRepository.findTransfersByEntityOrdered(identity, pageable.getOffset(), pageable.getPageSize());
    }

   // asset issuance

    public List<TransactionDto> getIssuedAssets(Pageable pageable) {
        return transactionsRepository.findByInputTypesOrdered(ISSUE_ASSET_INPUT_TYPE, pageable.getOffset(), pageable.getPageSize());
    }

}
