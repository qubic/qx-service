package org.qubic.qx.api.controller.domain;

import org.qubic.qx.api.db.domain.ExtraData;

import java.io.Serializable;

public record TransactionDto(
        String hash,
        String source,
        long amount,
        long tick,
        int inputType,
        ExtraData extraData,
        Boolean moneyFlew
) implements Serializable {}
