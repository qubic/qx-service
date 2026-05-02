package org.qubic.qx.sync.domain;

public record AssetOwnershipChange(String source,
                                   String destination,
                                   String assetIssuer,
                                   String assetName,
                                   long numberOfShares) {
}
