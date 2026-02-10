package org.qubic.qx.api.db.dto;

import java.io.Serializable;
import java.time.Instant;

public record AvgPriceData(Instant time,
                           long min,
                           long max,
                           long totalShares,
                           long totalAmount,
                           double averagePrice,
                           long totalTrades
) implements Serializable {
}
