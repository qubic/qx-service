package org.qubic.qx.sync.domain;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.util.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;

class QxIssueAssetDataTest {

    private static final String JSON = """
                        {"@class":".QxIssueAssetData","name":"name","numberOfShares":12345,"unitOfMeasurement":"ANAA0CMYFQ==","numberOfDecimalPlaces":8}""";

    @Test
    public void deserialize() {
        QxIssueAssetData data = new QxIssueAssetData("name", 12345, "ANAA0CMYFQ==", (byte) 8);
        String json = JsonUtil.toJson(data);
        assertThat(json).isEqualTo(JSON);
    }

    @Test
    public void serialize() {
        QxIssueAssetData data = JsonUtil.fromJson(JSON, QxIssueAssetData.class);
        assertThat(data).isEqualTo(new QxIssueAssetData("name", 12345, "ANAA0CMYFQ==", (byte) 8));
    }

}