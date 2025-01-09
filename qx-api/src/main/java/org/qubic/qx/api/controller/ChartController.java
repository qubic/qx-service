package org.qubic.qx.api.controller;

import org.qubic.qx.api.db.dto.AvgPriceData;
import org.qubic.qx.api.controller.service.ChartService;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.CACHE_KEY_ASSET;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_CHART_AVG_PRICE;

@Validated
@CrossOrigin
@RestController
@RequestMapping("/service/v1/qx")
public class ChartController {

    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @Cacheable(cacheNames = CACHE_NAME_CHART_AVG_PRICE, key = CACHE_KEY_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/chart/average-price")
    public List<AvgPriceData> getAveragePriceForAsset(@PathVariable("issuer") @Identity String issuer,
                                                      @PathVariable("asset") @AssetName String asset) {
        return chartService.getAveragePriceForAsset(issuer, asset);
    }

}
