package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.db.TradesRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class TradesService {

    private final TradesRepository tradesRepository;

    public TradesService(TradesRepository tradeRepository) {
        this.tradesRepository = tradeRepository;
    }

    public List<TradeDto> getTrades(Pageable pageable) {
        return tradesRepository.findAll(pageable.getOffset(), pageable.getPageSize());
    }

    public List<TradeDto> getSmartContractTrades(Pageable pageable) {
        return tradesRepository.findByIssuer(Asset.SMART_CONTRACT_ISSUER, pageable.getOffset(), pageable.getPageSize());
    }

    public List<TradeDto> getTokenTrades(Pageable pageable) {
        return tradesRepository.findByIssuerIsNot(Asset.SMART_CONTRACT_ISSUER, pageable.getOffset(), pageable.getPageSize());
    }

    public List<TradeDto> getAssetTrades(String issuer, String assetName, Pageable pageable) {
        return tradesRepository.findByIssuerAndAsset(issuer, assetName, pageable.getOffset(), pageable.getPageSize());
    }

    public List<TradeDto> getEntityTrades(String identity, Pageable pageable) {
        return tradesRepository.findByEntity(identity, pageable.getOffset(), pageable.getPageSize());
    }

}
