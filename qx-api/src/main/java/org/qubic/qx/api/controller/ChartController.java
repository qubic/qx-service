package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.domain.AvgPriceData;
import org.qubic.qx.api.controller.service.ChartService;
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
    public List<AvgPriceData> getAveragePriceForAsset(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                                      @PathVariable("asset") @Size(min = 1, max = 7) String assetName) {
        return chartService.getAveragePriceForAsset(issuer, assetName);
    }

}
