package org.qubic.qx.sync.domain;

public record QxTransferAssetData(String issuer, String name, String newOwner, long numberOfShares) implements ExtraData {

}
