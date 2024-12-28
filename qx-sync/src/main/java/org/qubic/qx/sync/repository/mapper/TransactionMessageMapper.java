package org.qubic.qx.sync.repository.mapper;

import org.qubic.qx.sync.domain.Transaction;
import org.qubic.qx.sync.repository.domain.TransactionMessage;

import java.time.Instant;

public class TransactionMessageMapper {

    public TransactionMessage map(Transaction transaction, Instant time, boolean assetEvents) {
        return new TransactionMessage(
                transaction.transactionHash(),
                transaction.sourcePublicId(),
                transaction.destinationPublicId(),
                transaction.amount(),
                transaction.tick(),
                time.getEpochSecond(),
                transaction.inputType(),
                transaction.inputSize(),
                transaction.extraData(),
                assetEvents
        );
    }

}
