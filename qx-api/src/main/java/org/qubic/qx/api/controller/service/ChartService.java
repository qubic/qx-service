package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.dto.AvgPriceData;
import org.qubic.qx.api.db.TradesRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ChartService {

    private static final int DAYS_OF_HISTORY_PER_DAY = 3 * 356;
    private static final int DAYS_OF_HISTORY_PER_HOUR = 356; // max 8544 entries

    private final TradesRepository tradesRepository;

    public ChartService(TradesRepository tradeRepository) {
        this.tradesRepository = tradeRepository;
    }

    public List<AvgPriceData> getAveragePriceForAssetPerDay(String issuer, String assetName) {
        return tradesRepository.findAveragePriceByAssetGroupedByDay(issuer, assetName,
                Instant.now().minus(Duration.ofDays(DAYS_OF_HISTORY_PER_DAY)));
    }

    public List<AvgPriceData> getAveragePriceForAssetPerHour(String issuer, String assetName) {
        return tradesRepository.findAveragePriceByAssetGroupedByHour(issuer, assetName,
                Instant.now().minus(Duration.ofDays(DAYS_OF_HISTORY_PER_HOUR)));
    }

}
