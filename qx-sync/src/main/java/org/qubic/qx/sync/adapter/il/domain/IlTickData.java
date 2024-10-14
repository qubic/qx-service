package org.qubic.qx.sync.adapter.il.domain;

import java.time.Instant;

public record IlTickData(int epoch, long tick, Instant timestamp) {
}
