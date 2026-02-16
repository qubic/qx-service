package org.qubic.qx.sync.adapter.il.domain.query;

public record IlQueryApiLastProcessedTick(long tickNumber, int epoch, long intervalInitialTick) {
}
