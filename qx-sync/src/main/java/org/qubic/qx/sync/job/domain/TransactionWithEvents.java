package org.qubic.qx.sync.job.domain;

import org.qubic.qx.sync.domain.TransactionEvent;
import org.qubic.qx.sync.domain.TransactionWithTime;

import java.util.List;

public record TransactionWithEvents(TransactionWithTime transaction, List<TransactionEvent> events) {
}
