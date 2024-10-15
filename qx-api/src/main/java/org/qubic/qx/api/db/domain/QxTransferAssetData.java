package org.qubic.qx.api.db.domain;

public record QxTransferAssetData(String issuer, String assetName, String newOwner, long numberOfUnits) implements ExtraData {

}
