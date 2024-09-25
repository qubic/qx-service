package org.qubic.qx.domain;

public record QxTransferAssetData(String issuer, String newOwner, String assetName, long numberOfUnits) implements ExtraData {

}
