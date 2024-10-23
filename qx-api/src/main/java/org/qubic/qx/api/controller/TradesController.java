package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.controller.service.TradesService;
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
    public List<TradeDto> getAssetTrades(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                         @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return tradesService.getAssetTrades(issuer, asset);
    }

    @Cacheable(CACHE_NAME_ENTITY_TRADES)
    @GetMapping("/entity/{identity}/trades")
    public List<TradeDto>  getEntityTrades(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return tradesService.getEntityTrades(identity);
    }

}
