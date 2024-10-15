package org.qubic.qx.sync.domain;

public record QxTransferAssetData(String issuer, String assetName, String newOwner, long numberOfUnits) implements ExtraData {

}
