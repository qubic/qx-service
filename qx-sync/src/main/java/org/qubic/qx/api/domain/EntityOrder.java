package org.qubic.qx.api.domain;

public record EntityOrder(String issuerId, String assetName, long price, long numberOfShares) {
}
