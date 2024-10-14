package org.qubic.qx.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.service.QxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/qx")
public class QxFunctionsController {

    private final QxService qxService;

    public QxFunctionsController(QxService qxService) {
        this.qxService = qxService;
    }

    @GetMapping("/fees")
    public Fees getFees() {
        return qxService.getFees();
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/orders/ask")
    public List<AssetOrder> getAssetAskOrders(@PathVariable("issuer") String issuer, @PathVariable("asset") String asset) {
        return qxService.getAssetAskOrders(issuer, asset);
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/orders/bid")
    public List<AssetOrder> getAssetBidOrders(@PathVariable("issuer") String issuer, @PathVariable("asset") String asset) {
        return qxService.getAssetBidOrders(issuer, asset);
    }

    @GetMapping("/entity/{identity}/orders/ask")
    public List<EntityOrder> getEntityAskOrders(@PathVariable("identity") String identity) {
        return qxService.getEntityAskOrders(identity);
    }

    @GetMapping("/entity/{identity}/orders/bid")
    public List<EntityOrder> getEntityBidOrders(@PathVariable("identity") String identity) {
        return qxService.getEntityBidOrders(identity);
    }

}

