package org.qubic.qx.sync.adapter.qubicj;

import at.qubic.api.service.ComputorService;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.qubicj.mapping.QubicjMapper;
import org.qubic.qx.sync.api.domain.AssetOrder;
import org.qubic.qx.sync.api.domain.EntityOrder;
import org.qubic.qx.sync.api.domain.Fees;
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
    public Mono<Fees> getFees() {
        return computorService.getQxFees()
                .map(qxMapper::map);
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

    @Override
    public Mono<List<EntityOrder>> getEntityAskOrders(String identity) {
        return computorService.getQxAskEntityOrders(identity, 0)
                .map(qxMapper::mapEntityOrders);
    }

    @Override
    public Mono<List<EntityOrder>> getEntityBidOrders(String identity) {
        return computorService.getQxBidEntityOrders(identity, 0)
                .map(qxMapper::mapEntityOrders);
    }

}
