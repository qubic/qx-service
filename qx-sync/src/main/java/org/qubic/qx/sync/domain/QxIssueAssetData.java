package org.qubic.qx.sync.domain;

public record QxIssueAssetData(String name, long numberOfUnits, String unitOfMeasurement, byte numberOfDecimalPlaces) implements ExtraData {

}
