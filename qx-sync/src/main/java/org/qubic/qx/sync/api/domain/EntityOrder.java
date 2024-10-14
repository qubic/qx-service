package org.qubic.qx.sync.api.domain;

public record EntityOrder(String issuerId, String assetName, long price, long numberOfShares) {
}
