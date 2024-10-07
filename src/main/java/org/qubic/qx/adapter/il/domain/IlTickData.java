package org.qubic.qx.adapter.il.domain;

import java.time.Instant;

public record IlTickData(int epoch, long tick, Instant timestamp) {
}
