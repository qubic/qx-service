package org.qubic.qx.api.db.domain;

public record QxAssetOrderData(String issuer, String name, long price, long numberOfShares) implements ExtraData {

}
