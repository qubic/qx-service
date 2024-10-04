package org.qubic.qx.domain;

public record Trade(long tick, String transactionHash, String maker, boolean bid, String issuer, String assetName, long numberOfShares, long price) {

}
