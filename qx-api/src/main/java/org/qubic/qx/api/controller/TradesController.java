package org.qubic.qx.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.controller.service.TradesService;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.qubic.qx.api.validation.Pagination;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.*;

@Slf4j
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
    public List<TradeDto> getTrades(@Pagination Pageable pageable) {
        return tradesService.getTrades(pageable);
    }

    @Cacheable(CACHE_NAME_TRADES_SMART_CONTRACTS)
    @GetMapping("/smart-contract-trades")
    public List<TradeDto> getSmartContractTrades(@Pagination Pageable pageable) {
        return tradesService.getSmartContractTrades(pageable);
    }

    @Cacheable(CACHE_NAME_TRADES_TOKENS)
    @GetMapping("/token-trades")
    public List<TradeDto> getTokenTrades(@Pagination Pageable pageable) {
        return tradesService.getTokenTrades(pageable);
    }

    @Cacheable(cacheNames = CACHE_NAME_ASSET_TRADES)
    @GetMapping("/issuer/{issuer}/asset/{asset}/trades")
    public List<TradeDto> getAssetTrades(@PathVariable("issuer") @Identity String issuer,
                                         @PathVariable("asset") @AssetName String asset,
                                         @Pagination Pageable pageable) {
        return tradesService.getAssetTrades(issuer, asset, pageable);
    }

    @Cacheable(CACHE_NAME_ENTITY_TRADES)
    @GetMapping("/entity/{identity}/trades")
    public List<TradeDto>  getEntityTrades(@PathVariable("identity") @Identity String identity,
                                           @Pagination Pageable pageable) {
        log.info("Key: {}", SimpleKeyGenerator.generateKey(identity, pageable));
        return tradesService.getEntityTrades(identity, pageable);
    }

}
