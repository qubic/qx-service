package org.qubic.qx.domain;

import org.qubic.qx.api.domain.AssetOrder;

import java.util.List;

public record OrderBook(long tickNumber, String issuer, String assetName, List<AssetOrder> asks, List<AssetOrder> bids) {
}
