package org.qubic.qx.domain;

public record QxAssetOrderData(String issuer, String name, long price, long numberOfShares) implements ExtraData {

}
