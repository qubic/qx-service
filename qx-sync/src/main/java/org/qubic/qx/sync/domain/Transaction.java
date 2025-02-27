package org.qubic.qx.sync.domain;

public record Transaction(
        String transactionHash,
        String sourcePublicId, // converted public key
        String destinationPublicId, // converted public key
        long amount, // should be treated as unsigned long
        long tick,
        int inputType, // operation type
        int inputSize, // extra data size
        ExtraData extraData // hex
) { }
