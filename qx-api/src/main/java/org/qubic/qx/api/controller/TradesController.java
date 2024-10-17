package org.qubic.qx.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.controller.service.TradesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/service/v1/qx")
public class TradesController {

    private final TradesService tradesService;

    public TradesController(TradesService tradesService) {
        this.tradesService = tradesService;
    }

    @GetMapping("/trades")
    public List<TradeDto> getTrades() {
        return tradesService.getTrades();
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/trades")
    public List<TradeDto> getAssetTrades(@PathVariable("issuer") String issuer, @PathVariable("asset") String asset) {
        return tradesService.getAssetTrades(issuer, asset);
    }

    @GetMapping("/entity/{identity}/trades")
    public List<TradeDto>  getEntityTrades(@PathVariable("identity") String identity) {
        return tradesService.getEntityTrades(identity);
    }

}
