package org.qubic.qx.sync.adapter.qubicj;

import at.qubic.api.service.ComputorService;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.qubicj.mapping.QubicjMapper;
import org.qubic.qx.sync.domain.AssetOrder;
import reactor.core.publisher.Mono;

import java.util.List;

public class QubicjQxApiService implements QxApiService {

    private final ComputorService computorService;
    private final QubicjMapper qxMapper;

    public QubicjQxApiService(ComputorService computorService, QubicjMapper qxMapper) {
        this.computorService = computorService;
        this.qxMapper = qxMapper;
    }

    @Override
    public Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset) {
        return computorService.getQxAskAssetOrders(issuer, asset, 0)
                .map(qxMapper::mapAssetOrders);
    }

    @Override
    public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return computorService.getQxBidAssetOrders(issuer, asset, 0)
                .map(qxMapper::mapAssetOrders);
    }

}
