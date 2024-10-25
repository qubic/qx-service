package org.qubic.qx.api.db.domain;

import java.io.Serializable;

public record QxAssetOrderData(String issuer, String name, long price, long numberOfShares) implements ExtraData, Serializable {

}
