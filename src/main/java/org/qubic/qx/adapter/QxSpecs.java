package org.qubic.qx.adapter;

import java.util.Map;

public class QxSpecs {

    public static final byte[] QX_PUBLIC_KEY = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    public static final String QX_PUBLIC_ID = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID";

    public static final Map<Integer, String> INPUT_TYPES = Map.of(1, "issue asset",
            2, "transfer share",
            5, "add ask order",
            6, "add bid order",
            7, "remove ask order",
            8, "remove bid order");

}
