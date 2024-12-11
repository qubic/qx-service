package org.qubic.qx.sync.domain;

public record TransactionEvent(EventHeader header, int eventType, long eventSize, String eventData) {
}
