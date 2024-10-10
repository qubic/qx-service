package org.qubic.qx.domain;

public record Trade(long tick,
                    long timestamp,
                    String transactionHash,
                    boolean bid, String taker,
                    String maker,
                    String issuer,
                    String assetName,
                    long price, long numberOfShares
)
{}
