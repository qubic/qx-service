package org.qubic.qx.api.controller.domain;

public record EntityOrder(String issuerId, String assetName, long price, long numberOfShares) {
}
