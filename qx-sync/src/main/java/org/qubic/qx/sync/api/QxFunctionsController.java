package org.qubic.qx.sync.api;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.api.domain.AssetOrder;
import org.qubic.qx.sync.api.domain.EntityOrder;
import org.qubic.qx.sync.api.domain.Fees;
import org.qubic.qx.sync.api.service.QxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public Mono<Fees> getFees() {
        return qxService.getFees();
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/orders/ask")
    public Mono<List<AssetOrder>> getAssetAskOrders(@PathVariable("issuer") String issuer, @PathVariable("asset") String asset) {
        return qxService.getAssetAskOrders(issuer, asset);
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/orders/bid")
    public Mono<List<AssetOrder>> getAssetBidOrders(@PathVariable("issuer") String issuer, @PathVariable("asset") String asset) {
        return qxService.getAssetBidOrders(issuer, asset);
    }

    @GetMapping("/entity/{identity}/orders/ask")
    public Mono<List<EntityOrder>> getEntityAskOrders(@PathVariable("identity") String identity) {
        return qxService.getEntityAskOrders(identity);
    }

    @GetMapping("/entity/{identity}/orders/bid")
    public Mono<List<EntityOrder>> getEntityBidOrders(@PathVariable("identity") String identity) {
        return qxService.getEntityBidOrders(identity);
    }

}

