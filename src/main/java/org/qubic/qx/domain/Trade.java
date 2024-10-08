package org.qubic.qx.domain;

public record Trade(long tick,
                    String transactionHash,
                    String taker,
                    String maker,
                    boolean bid,
                    String issuer,
                    String assetName,
                    long numberOfShares,
                    long price)
{}
