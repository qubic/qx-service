package org.qubic.qx.sync.domain;

public record EventHeader(int epoch, long tick, String eventId, String eventDigest) {
}
