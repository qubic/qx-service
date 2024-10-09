package org.qubic.qx.assets;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.domain.OrderBook;
import org.qubic.qx.repository.OrderBookRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

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

    /**
     * Retrieves order books for all known assets. Needs the <b>current</b> tick number as input.
     * @param currentTickNumber The current tick number. Order books that are not stored will be retrieved from the network.
     * @return Flux of order books for all assets.
     */
    public Flux<OrderBook> retrieveAllCurrentOrderBooks(long currentTickNumber) {
        return retrieveCurrentOrderBooks(currentTickNumber, assets.getAssets());
    }

    /**
     * Retrieves the order books for the specified assets. Needs the <b>current</b> tick number as input.
     * @param currentTickNumber The current tick number. Order books that are not stored will be retrieved from the network.
     * @param assetSet The assets to retrieve the order books for.
     * @return Flux of order books for all specified assets.
     */
    public Flux<OrderBook> retrieveCurrentOrderBooks(long currentTickNumber, Set<Asset> assetSet) {
        return Flux.fromIterable(assetSet)
                .flatMap(asset -> orderBookRepository.getOrderBook(asset.issuer(), asset.name(), currentTickNumber)
                        .switchIfEmpty(retrieveOrderBook(currentTickNumber, asset).flatMap(ob -> orderBookRepository.storeOrderBook(ob).map(x -> ob)))
                        .doOnNext(ob -> log.debug("[{}] order book: {}", ob.assetName(), ob)));
    }

    private Mono<OrderBook> retrieveOrderBook(long tickNumber, Asset asset) {
        Mono<List<AssetOrder>> asks = qxApiService.getAssetAskOrders(asset.issuer(), asset.name()).retry(3);
        Mono<List<AssetOrder>> bids = qxApiService.getAssetBidOrders(asset.issuer(), asset.name()).retry(3);
        return Mono.zip(asks, bids).map(t2 -> new OrderBook(tickNumber, asset.issuer(), asset.name(), t2.getT1(), t2.getT2()));
    }

    public Flux<OrderBook> loadLatestOrderBooksBeforeTick(long tickNumber, Set<Asset> assetSet) {
        return Flux.fromIterable(assetSet)
                .flatMap(asset -> orderBookRepository.getPreviousOrderBookBefore(asset.issuer(), asset.name(), tickNumber));
    }
}
