package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.db.TradesRepository;

import java.util.List;

public class TradesService {

    private final TradesRepository tradesRepository;

    public TradesService(TradesRepository tradeRepository) {
        this.tradesRepository = tradeRepository;
    }

    public List<TradeDto> getTrades() {
        return tradesRepository.findOrderedByTickTimeDesc(100);
    }

    public List<TradeDto> getAssetTrades(String issuer, String assetName) {
        return tradesRepository.findByAssetOrderedByTickTimeDesc(issuer, assetName, 100);
    }

    public List<TradeDto> getEntityTrades(String identity) {
        return tradesRepository.findByEntityOrderedByTickTimeDesc(identity, 100);
    }

}
