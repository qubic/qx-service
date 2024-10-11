package org.qubic.qx.api.service;

import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.domain.Trade;
import org.qubic.qx.repository.TradeRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TradesService {

    private final TradeRepository tradeRepository;

    public TradesService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Flux<Trade> getTrades() {
        return tradeRepository.findTrades(Instant.now().minus(10, ChronoUnit.DAYS), Instant.now())
                .take(100);
    }

    public Flux<Trade> getAssetTrades(String issuer, String asset) {
        return getTrades()
                .filter(t -> StringUtils.equals(issuer, t.issuer()) && StringUtils.equals(asset, t.assetName()));
    }

    public Flux<Trade> getEntityTrades(String entity) {
        return getTrades()
                .filter(t -> StringUtils.equalsAny(entity, t.maker(), t.taker()));
    }

}
