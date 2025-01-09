package org.qubic.qx.api.controller.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.qubic.qx.api.validation.Identity;

import java.math.BigInteger;

public record QxOrderRequest(

        @Identity
        String from,

        @NotNull
        @Positive
        BigInteger numberOfShares,

        @NotNull
        @Positive
        BigInteger pricePerShare

) { }
