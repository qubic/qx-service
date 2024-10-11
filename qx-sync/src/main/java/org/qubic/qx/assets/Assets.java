package org.qubic.qx.assets;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Assets {

    @Getter
    private final Set<Asset> assets = new HashSet<>();

    public void add(Asset asset) {
        log.info("Adding asset: {}", asset);
        assets.add(asset);
    }

}
