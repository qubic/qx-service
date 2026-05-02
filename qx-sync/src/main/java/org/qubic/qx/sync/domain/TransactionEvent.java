package org.qubic.qx.sync.domain;

import lombok.Data;

@Data
public class TransactionEvent {

    private final EventHeader header;
    private final int eventType;
    private final long eventSize;
    private final String eventData;

}
