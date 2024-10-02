package org.qubic.qx.adapter.qubicj;

import at.qubic.api.service.ComputorService;
import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.adapter.qubicj.mapping.QubicjOxMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import reactor.core.publisher.Mono;

import java.util.List;

public class QubicjQxApiService implements QxApiService {

    private final ComputorService computorService;
    private final QubicjOxMapper qxMapper;

    public QubicjQxApiService(ComputorService computorService, QubicjOxMapper qxMapper) {
        this.computorService = computorService;
        this.qxMapper = qxMapper;
    }

    @Override
    public Mono<Fees> getFees() {
        return computorService.getQxFees()
                .map(qxMapper::mapFees);
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
