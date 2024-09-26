package org.qubic.qx.api;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.assets.Asset;
import org.qubic.qx.assets.Assets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/v1/qx")
public class QxAssetsController {

    private final Assets assets;

    public QxAssetsController(Assets assets) {
        this.assets = assets;
    }

    @GetMapping("/assets")
    public Flux<Asset> getAssets() {
        return Flux.fromIterable(assets.getAssets());
    }

}

