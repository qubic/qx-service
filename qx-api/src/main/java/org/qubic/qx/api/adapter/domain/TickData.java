package org.qubic.qx.api.adapter.domain;

import java.time.Instant;

public record TickData(int epoch, long tick, Instant timestamp) {
}
