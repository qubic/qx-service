package org.qubic.qx.api.controller;

import org.apache.commons.lang3.Strings;
import org.qubic.qx.api.controller.domain.ChartInterval;
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

    @Cacheable(cacheNames = CACHE_NAME_CHART_AVG_PRICE)
    @GetMapping("/issuer/{issuer}/asset/{asset}/chart/average-price")
    public List<AvgPriceData> getAveragePriceForAsset(@PathVariable @Identity String issuer,
                                                      @PathVariable @AssetName String asset,
                                                      @RequestParam(name = "interval", required = false) ChartInterval interval) {
        if (interval == ChartInterval.HOUR) {
            return chartService.getAveragePriceForAssetPerHour(issuer, asset);
        } else {
            return chartService.getAveragePriceForAssetPerDay(issuer, asset);
        }
    }

}
