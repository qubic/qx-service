package org.qubic.qx.api.db.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record AvgPriceData(LocalDate time,
                           long min,
                           long max,
                           long totalShares,
                           long totalAmount,
                           double averagePrice,
                           long totalTrades
) implements Serializable {
}
