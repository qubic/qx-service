package org.qubic.qx.api.controller.domain;

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
