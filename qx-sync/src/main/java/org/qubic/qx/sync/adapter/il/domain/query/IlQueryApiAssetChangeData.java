package org.qubic.qx.sync.adapter.il.domain.query;

public record IlQueryApiAssetChangeData(String source,
                                        String destination,
                                        String assetIssuer,
                                        String assetName,
                                        long numberOfShares) {
}
