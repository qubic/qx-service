package org.qubic.qx.api.controller.domain;

public enum ChartInterval {
    HOUR("1h"), DAY("1d");

    private final String key;

    ChartInterval(String key) {
        this.key = key;
    }

    public static ChartInterval fromKey(String key) {
        for (ChartInterval interval : values()) {
            if (interval.key.equals(key)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Invalid chart interval: " + key);
    }

}
