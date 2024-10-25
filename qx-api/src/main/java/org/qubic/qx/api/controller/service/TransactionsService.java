package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.db.TransactionsRepository;

import java.util.List;

public class TransactionsService {

    static final int LIMIT = 25;
    static final List<Integer> ORDER_INPUT_TYPES = List.of(5,6,7,8);
    static final List<Integer> TRANSFER_INPUT_TYPE = List.of(2);

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

    // orders

    public List<TransactionDto> getOrderTransactions() {
        return transactionsRepository.findByInputTypesOrdered(ORDER_INPUT_TYPES, LIMIT);
    }

    public List<TransactionDto> getOrderTransactionsForAsset(String issuer, String asset) {
        return transactionsRepository.findByAssetOrdered(issuer, asset, ORDER_INPUT_TYPES, LIMIT);
    }

    public List<TransactionDto> getOrderTransactionsForEntity(String identity) {
        return transactionsRepository.findBySourceEntityOrdered(identity, ORDER_INPUT_TYPES, LIMIT);
    }

}
