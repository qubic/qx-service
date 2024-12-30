package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.db.TradesRepository;

import java.util.List;

public class TradesService {

    private final TradesRepository tradesRepository;

    public TradesService(TradesRepository tradeRepository) {
        this.tradesRepository = tradeRepository;
    }

    public List<TradeDto> getTrades() {
        return tradesRepository.findOrderedByTickTimeDesc(50);
    }

    public List<TradeDto> getAssetTrades(String issuer, String assetName) {
        return tradesRepository.findByAssetOrderedByTickTimeDesc(issuer, assetName, 50);
    }

    public List<TradeDto> getEntityTrades(String identity) {
        return tradesRepository.findByEntityOrderedByTickTimeDesc(identity, 50);
    }

}
