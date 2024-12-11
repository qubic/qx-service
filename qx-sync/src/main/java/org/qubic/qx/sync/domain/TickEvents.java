package org.qubic.qx.sync.domain;

import java.util.List;

public record TickEvents(long tick, List<TransactionEvents> txEvents) {
}
