package org.qubic.qx.api.db.dto;

import java.io.Serializable;
import java.math.BigInteger;

public record AmountPerEntityDto(
        String identity, BigInteger amount
) implements Serializable { }
