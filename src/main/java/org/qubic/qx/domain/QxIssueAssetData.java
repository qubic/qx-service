package org.qubic.qx.domain;

public record QxIssueAssetData(String name, long numberOfUnits, String unitOfMeasurement, byte numberOfDecimalPlaces) implements ExtraData {

}
