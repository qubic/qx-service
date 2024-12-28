package org.qubic.qx.sync.repository.domain;

import org.qubic.qx.sync.domain.ExtraData;

public record TransactionMessage(
        String transactionHash,
        String sourcePublicId,
        String destinationPublicId,
        long amount,
        long tick,
        long timestamp,
        int inputType,
        int inputSize,
        ExtraData extraData,
        boolean relevantEvents
) { }
