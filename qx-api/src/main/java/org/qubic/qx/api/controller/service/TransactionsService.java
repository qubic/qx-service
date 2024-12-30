package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.db.TransactionsRepository;

import java.util.List;

public class TransactionsService {

    static final int LIMIT = 50;
    static final List<Integer> TRANSFER_INPUT_TYPE = List.of(2);
    static final List<Integer> ISSUE_ASSET_INPUT_TYPE = List.of(1);

    private final TransactionsRepository transactionsRepository;

    public TransactionsService(TransactionsRepository transactionsRepository) {
        this.transactionsRepository = transactionsRepository;
    }

    // transfers

    public List<TransactionDto> getTransferTransactions() {
        return transactionsRepository.findByInputTypesOrdered(TRANSFER_INPUT_TYPE, 50);
    }

    public List<TransactionDto> getTransferTransactionsForAsset(String issuer, String asset) {
        return transactionsRepository.findByAssetOrdered(issuer, asset, TRANSFER_INPUT_TYPE, LIMIT);
    }

    public List<TransactionDto> getTransferTransactionsForEntity(String identity) {
        return transactionsRepository.findTransfersByEntityOrdered(identity, LIMIT);
    }

   // asset issuance

    public List<TransactionDto> getIssuedAssets() {
        return transactionsRepository.findByInputTypesOrdered(ISSUE_ASSET_INPUT_TYPE, LIMIT);
    }

}
