package org.qubic.qx.api.redis.dto;

public record TradeRedisDto(long tick,
                           long timestamp,
                           String transactionHash,
                           boolean bid,
                           String taker,
                           String maker,
                           String issuer,
                           String assetName,
                           long price,
                           long numberOfShares
)
{}
