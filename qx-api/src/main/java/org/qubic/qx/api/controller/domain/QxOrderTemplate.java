package org.qubic.qx.api.controller.domain;

import java.math.BigInteger;

public record QxOrderTemplate(
        BigInteger currentTick,
        String from,
        String to,
        int inputType,
        BigInteger amount,
        String extraData) {
}
