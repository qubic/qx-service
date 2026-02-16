package org.qubic.qx.sync.adapter.il.domain.goqubic;

import java.time.Instant;

public record IlTickData(int epoch, long tick, Instant timestamp) {
}
