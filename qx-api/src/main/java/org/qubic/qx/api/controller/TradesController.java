package org.qubic.qx.api.controller;

import org.qubic.qx.api.controller.service.TradesService;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.*;

@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class TradesController {

    private final TradesService tradesService;

    public TradesController(TradesService tradesService) {
        this.tradesService = tradesService;
    }

    @Cacheable(CACHE_NAME_TRADES)
    @GetMapping("/trades")
    public List<TradeDto> getTrades() {
        return tradesService.getTrades();
    }

    @Cacheable(cacheNames = CACHE_NAME_ASSET_TRADES, key = CACHE_KEY_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/trades")
    public List<TradeDto> getAssetTrades(@PathVariable("issuer") @Identity String issuer,
                                         @PathVariable("asset") @AssetName String asset) {
        return tradesService.getAssetTrades(issuer, asset);
    }

    @Cacheable(CACHE_NAME_ENTITY_TRADES)
    @GetMapping("/entity/{identity}/trades")
    public List<TradeDto>  getEntityTrades(@PathVariable("identity") @Identity String identity) {
        return tradesService.getEntityTrades(identity);
    }

}
