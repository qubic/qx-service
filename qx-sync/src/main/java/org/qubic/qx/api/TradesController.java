package org.qubic.qx.api;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.service.TradesService;
import org.qubic.qx.domain.Trade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/v1/qx")
public class TradesController {

    private final TradesService tradesService;

    public TradesController(TradesService tradesService) {
        this.tradesService = tradesService;
    }

    @GetMapping("/trades")
    public Flux<Trade> getTrades() {
        return tradesService.getTrades();
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/trades")
    public Flux<Trade> getAssetTrades(@PathVariable("issuer") String issuer, @PathVariable("asset") String asset) {
        return tradesService.getAssetTrades(issuer, asset);
    }

    @GetMapping("/entity/{identity}/trades")
    public Flux<Trade>  getEntityTrades(@PathVariable("identity") String identity) {
        return tradesService.getEntityTrades(identity);
    }

}
