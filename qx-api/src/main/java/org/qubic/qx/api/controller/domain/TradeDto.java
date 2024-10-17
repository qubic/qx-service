package org.qubic.qx.api.controller.domain;

import java.time.Instant;

public record TradeDto(Instant tickTime,
                       String transactionHash,
                       String taker,
                       String maker,
                       String issuer,
                       String assetName,
                       boolean bid,
                       long price,
                       long numberOfShares
)
{}
