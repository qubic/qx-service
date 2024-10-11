package org.qubic.qx.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class AssetUtil {

    public static String getAssetNameString(byte[] assetName) {
        return assetName == null ? StringUtils.EMPTY : new String(assetName, StandardCharsets.US_ASCII).trim();
    }

    public static String getUnitOfMeasurementString(byte[] input) {
        assert input.length >= 7;
        byte[] converted = new byte[7];
        // convert 0-9 to ASCII 0-9 (decimal 48-57) by adding 48. 0000020 is then shown as "0000020" for example.
        for (int i = 0; i < 7; i++) { converted[i] = (byte) (input[i] + 48); }
        return new String(converted, StandardCharsets.US_ASCII).trim();
    }

}
