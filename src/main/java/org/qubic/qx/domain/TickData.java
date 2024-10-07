package org.qubic.qx.domain;

import java.time.Instant;

public record TickData(int epoch, long tick, Instant timestamp) {

}
