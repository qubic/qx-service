package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.controller.domain.AvgPriceData;
import org.qubic.qx.api.db.TradesRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ChartService {

    private final TradesRepository tradesRepository;

    public ChartService(TradesRepository tradeRepository) {
        this.tradesRepository = tradeRepository;
    }

    public List<AvgPriceData> getAveragePriceForAsset(String issuer, String assetName) {
        return tradesRepository.findAveragePriceByAssetGroupedByDay(issuer, assetName, Instant.now().minus(Duration.ofDays(365)));
    }

}
