package org.qubic.qx.assets;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.domain.OrderBook;
import org.qubic.qx.repository.OrderBookRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class AssetService {

    private final Assets assets;
    private final QxApiService qxApiService;
    private final OrderBookRepository orderBookRepository;

    public AssetService(Assets assets, QxApiService qxApiService, OrderBookRepository orderBookRepository) {
        this.assets = assets;
        this.qxApiService = qxApiService;
        this.orderBookRepository = orderBookRepository;
    }

    public Flux<Long> updateOrderBooks(long tickNumber) {
        List<Mono<OrderBook>> orderBooks = new ArrayList<>();
        for (Asset asset : assets.getAssets()) {
            Mono<List<AssetOrder>> asks = qxApiService.getAssetAskOrders(asset.issuer(), asset.name()).retry(3);
            Mono<List<AssetOrder>> bids = qxApiService.getAssetBidOrders(asset.issuer(), asset.name()).retry(3);
            Mono<OrderBook> orderBook = Mono.zip(asks, bids)
                    .map(t2 -> new OrderBook(tickNumber, asset.issuer(), asset.name(), t2.getT1(), t2.getT2()));
            orderBooks.add(orderBook);
        }
        return Flux.fromIterable(orderBooks)
                .flatMap(ob -> ob)
                .doOnNext(ob -> log.info("{}", ob))
                .flatMap(orderBookRepository::storeOrderBook);
    }

}
