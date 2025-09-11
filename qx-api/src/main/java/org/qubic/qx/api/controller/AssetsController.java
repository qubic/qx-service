package org.qubic.qx.api.controller;

import org.qubic.qx.api.controller.service.AssetsService;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/service/v1/qx")
public class AssetsController {

    private final AssetsService assetsService;

    public AssetsController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @GetMapping("/assets")
    public List<Asset> getAssets(@RequestParam(name = "all", defaultValue = "false") boolean all) {
        if (all) {
            return assetsService.getAllAssets();
        } else {
            return assetsService.getVerifiedAssets();
        }
    }

}

