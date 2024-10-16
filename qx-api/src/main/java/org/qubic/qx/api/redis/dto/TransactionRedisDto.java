package org.qubic.qx.api.redis.dto;

import org.qubic.qx.api.db.domain.ExtraData;

public record TransactionRedisDto(
        String transactionHash,
        String sourcePublicId,
        String destinationPublicId,
        long amount,
        long tick,
        int inputType,
        int inputSize,
        ExtraData extraData, // compatible with domain object
        Boolean moneyFlew
) { }
