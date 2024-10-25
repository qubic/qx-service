package org.qubic.qx.api.db.domain;

import java.io.Serializable;

public record QxIssueAssetData(String name, long numberOfShares, String unitOfMeasurement, byte numberOfDecimalPlaces) implements ExtraData, Serializable {

}
