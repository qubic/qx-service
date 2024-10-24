package org.qubic.qx.api.db.domain;

public record QxTransferAssetData(String issuer, String name, String newOwner, long numberOfShares) implements ExtraData {

}
