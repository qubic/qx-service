package org.qubic.qx.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.controller.service.QxService;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.*;

@Slf4j
@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class QxFunctionsController {

    private final QxService qxService;

    public QxFunctionsController(QxService qxService) {
        this.qxService = qxService;
    }

    @Cacheable(CACHE_NAME_FEES)
    @GetMapping("/fees")
    public Fees getFees() {
        return qxService.getFees();
    }

    @Cacheable(cacheNames = CACHE_NAME_ASSET_ASKS, key = CACHE_KEY_ASSET_AGGREGATED)
    @GetMapping("/issuer/{issuer}/asset/{asset}/asks")
    public List<AssetOrder> getAssetAskOrders(@PathVariable("issuer") @Identity String issuer,
                                              @PathVariable("asset") @AssetName String asset,
                                              @RequestParam(value = "aggregated", defaultValue = "false") boolean aggregated) {
        return aggregated
                ? qxService.getAggregatedAssetAskOrders(issuer, asset)
                : qxService.getAssetAskOrders(issuer, asset);
    }

    @Cacheable(cacheNames = CACHE_NAME_ASSET_BIDS, key = CACHE_KEY_ASSET_AGGREGATED)
    @GetMapping("/issuer/{issuer}/asset/{asset}/bids")
    public List<AssetOrder> getAssetBidOrders(@PathVariable("issuer") @Identity String issuer,
                                              @PathVariable("asset") @AssetName String asset,
                                              @RequestParam(value = "aggregated", defaultValue = "false") boolean aggregated) {
        return aggregated
                ? qxService.getAggregatedAssetBidOrders(issuer, asset)
                : qxService.getAssetBidOrders(issuer, asset);
    }

    @Cacheable(CACHE_NAME_ENTITY_ASKS)
    @GetMapping("/entity/{identity}/asks")
    public List<EntityOrder> getEntityAskOrders(@PathVariable("identity") @Identity String identity) {
        return qxService.getEntityAskOrders(identity);
    }

    @Cacheable(CACHE_NAME_ENTITY_BIDS)
    @GetMapping("/entity/{identity}/bids")
    public List<EntityOrder> getEntityBidOrders(@PathVariable("identity") @Identity String identity) {
        return qxService.getEntityBidOrders(identity);
    }

}

