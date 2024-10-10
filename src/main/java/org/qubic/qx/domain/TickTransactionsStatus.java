package org.qubic.qx.domain;

import java.util.Map;

public record TickTransactionsStatus(long tick, int txCount, Map<String, Boolean> statusPerTx) {
}
