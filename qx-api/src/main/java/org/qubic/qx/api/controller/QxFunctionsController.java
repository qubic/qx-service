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

@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class QxFunctionsController {

    private final QxService qxService;

    public QxFunctionsController(QxService qxService) {
        this.qxService = qxService;
    }

    @Cacheable("fees")
    @GetMapping("/fees")
    public Fees getFees() {
        return qxService.getFees();
    }

    @Cacheable("assetAskOrders")
    @GetMapping("/issuer/{issuer}/asset/{asset}/orders/ask")
    public List<AssetOrder> getAssetAskOrders(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                              @PathVariable("asset") @Size(min=1, max=7) String asset) {
        return qxService.getAssetAskOrders(issuer, asset);
    }

    @Cacheable("assetBidOrders")
    @GetMapping("/issuer/{issuer}/asset/{asset}/orders/bid")
    public List<AssetOrder> getAssetBidOrders(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                              @PathVariable("asset") @Size(min=1, max=7) String asset) {
        return qxService.getAssetBidOrders(issuer, asset);
    }

    @Cacheable("entityAskOrders")
    @GetMapping("/entity/{identity}/orders/ask")
    public List<EntityOrder> getEntityAskOrders(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return qxService.getEntityAskOrders(identity);
    }

    @Cacheable("entityBidOrders")
    @GetMapping("/entity/{identity}/orders/bid")
    public List<EntityOrder> getEntityBidOrders(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return qxService.getEntityBidOrders(identity);
    }

}

