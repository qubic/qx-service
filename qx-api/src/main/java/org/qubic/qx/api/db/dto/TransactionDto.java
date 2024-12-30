package org.qubic.qx.api.db.dto;

import org.qubic.qx.api.db.domain.ExtraData;

import java.io.Serializable;
import java.time.Instant;

public record TransactionDto(
        Instant tickTime,
        String hash,
        String source,
        long amount,
        long tick,
        int inputType,
        ExtraData extraData,
        Boolean moneyFlew
) implements Serializable {}
