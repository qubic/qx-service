package org.qubic.qx.sync.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AssetUtil {

    public static String getAssetNameString(byte[] assetName) {
        return assetName == null ? StringUtils.EMPTY : new String(assetName, StandardCharsets.US_ASCII).trim();
    }

    public static String getUnitOfMeasurementString(byte[] input) {
        assert input.length == 7;
        return Base64.getEncoder().encodeToString(input);
    }

}
