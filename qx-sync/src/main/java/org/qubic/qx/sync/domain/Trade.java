package org.qubic.qx.sync.domain;

public record Trade(long tick,
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
