package org.qubic.qx.sync.domain;

import org.qubic.qx.sync.api.domain.AssetOrder;

import java.util.List;

public record OrderBook(long tickNumber, String issuer, String assetName, List<AssetOrder> asks, List<AssetOrder> bids) {
}
