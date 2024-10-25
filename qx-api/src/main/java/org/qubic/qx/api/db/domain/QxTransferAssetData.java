package org.qubic.qx.api.db.domain;

import java.io.Serializable;

public record QxTransferAssetData(String issuer, String name, String newOwner, long numberOfShares) implements ExtraData, Serializable {

}
