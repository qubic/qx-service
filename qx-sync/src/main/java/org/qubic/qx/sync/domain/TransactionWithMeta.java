package org.qubic.qx.sync.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
@Data
public class TransactionWithMeta {

    private final Transaction transaction;
    private final Instant time;
    private final List<TransactionEvent> events;

    public String transactionHash() {
        return transaction.transactionHash();
    }

    public String destinationPublicId() {
        return transaction.destinationPublicId();
    }

    public String sourcePublicId() {
        return transaction.sourcePublicId();
    }

    public int inputSize() {
        return transaction.inputSize();
    }

    public ExtraData extraData() {
        return transaction.extraData();
    }

    public int inputType() {
        return transaction.inputType();
    }

    public long amount() {
        return transaction.amount();
    }

    public long tick() {
        return transaction.tick();
    }

}
