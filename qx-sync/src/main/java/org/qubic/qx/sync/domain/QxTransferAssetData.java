package org.qubic.qx.sync.domain;

public record QxTransferAssetData(String issuer, String newOwner, String assetName, long numberOfUnits) implements ExtraData {

}
