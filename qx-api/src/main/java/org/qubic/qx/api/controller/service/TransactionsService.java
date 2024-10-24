package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.db.TransactionsRepository;

import java.util.List;

public class TransactionsService {

    private final TransactionsRepository transactionsRepository;

    public TransactionsService(TransactionsRepository transactionsRepository) {
        this.transactionsRepository = transactionsRepository;
    }

    public List<TransactionDto> getTransactions() {
        return transactionsRepository.findOrdered(50);
    }

    public List<TransactionDto> getTransactionsForAsset(String issuer, String asset) {
        return transactionsRepository.findByAssetOrdered(issuer, asset, 50);
    }

    public List<TransactionDto> getTransactionsForTypes(List<Integer> types) {
        return transactionsRepository.findByInputTypesOrdered(types, 50);
    }

    public List<TransactionDto> getTransactionsForEntity(String identity) {
        return transactionsRepository.findByEntityOrdered(identity, 50);
    }
}
