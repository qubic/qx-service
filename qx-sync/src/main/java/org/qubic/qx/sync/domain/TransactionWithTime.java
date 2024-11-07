package org.qubic.qx.sync.domain;

public record TransactionWithTime(
        String transactionHash,
        String sourcePublicId, // converted public key
        String destinationPublicId, // converted public key
        long amount, // should be treated as unsigned long
        long tick,
        long timestamp,
        int inputType, // operation type
        int inputSize, // extra data size
        ExtraData extraData, // hex
        Boolean moneyFlew
) { }
