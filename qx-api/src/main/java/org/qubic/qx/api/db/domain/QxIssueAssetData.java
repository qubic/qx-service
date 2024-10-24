package org.qubic.qx.api.db.domain;

public record QxIssueAssetData(String name, long numberOfShares, String unitOfMeasurement, byte numberOfDecimalPlaces) implements ExtraData {

}
