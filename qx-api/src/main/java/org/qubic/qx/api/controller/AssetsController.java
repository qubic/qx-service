package org.qubic.qx.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.controller.domain.Asset;
import org.qubic.qx.api.controller.service.AssetsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/service/v1/qx")
public class AssetsController {

    private final AssetsService assetsService;

    public AssetsController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @GetMapping("/assets")
    public List<Asset> getAssets() {
        return assetsService.getAssets();
    }

}

