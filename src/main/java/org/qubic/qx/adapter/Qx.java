package org.qubic.qx.adapter;

import java.util.*;
import java.util.stream.Collectors;

import static org.qubic.qx.adapter.Qx.OrderType.*;

public class Qx {

    public static final byte[] QX_PUBLIC_KEY = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    public static final String QX_PUBLIC_ID = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID";

    public enum OrderType {
        ISSUE_ASSET(1),
        TRANSFER_SHARE(2),
        ADD_ASK(5),
        ADD_BID(6),
        REMOVE_ASK(7),
        REMOVE_BID(8);

        public final int code;

        OrderType(int code) {
            this.code = code;
        }

        public static OrderType fromCode(int code) {
            for (OrderType orderType : values()) {
                if (orderType.code == code) {
                    return orderType;
                }
            }
            throw new IllegalArgumentException("Invalid order code: " + code);
        }
    }

    public static final Set<Integer> ALL_INPUT_TYPES = Arrays.stream(values())
            .map(o -> o.code)
            .collect(Collectors.toSet());

}
