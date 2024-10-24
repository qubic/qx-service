package org.qubic.qx.sync.domain;

public record QxIssueAssetData(String name, long numberOfShares, String unitOfMeasurement, byte numberOfDecimalPlaces) implements ExtraData {

}
