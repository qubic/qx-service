package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.controller.service.QxService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.*;

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

    @Cacheable(cacheNames = CACHE_NAME_ASSET_ASKS, key = CACHE_KEY_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/asks")
    public List<AssetOrder> getAssetAskOrders(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                              @PathVariable("asset") @Size(min=1, max=7) String asset) {
        return qxService.getAssetAskOrders(issuer, asset);
    }

    @Cacheable(cacheNames = CACHE_NAME_ASSET_BIDS, key = CACHE_KEY_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/bids")
    public List<AssetOrder> getAssetBidOrders(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                              @PathVariable("asset") @Size(min=1, max=7) String asset) {
        return qxService.getAssetBidOrders(issuer, asset);
    }

    @Cacheable(CACHE_NAME_ENTITY_ASKS)
    @GetMapping("/entity/{identity}/asks")
    public List<EntityOrder> getEntityAskOrders(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return qxService.getEntityAskOrders(identity);
    }

    @Cacheable(CACHE_NAME_ENTITY_BIDS)
    @GetMapping("/entity/{identity}/bids")
    public List<EntityOrder> getEntityBidOrders(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return qxService.getEntityBidOrders(identity);
    }

}

