package org.qubic.qx.sync.assets;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.domain.AssetOrder;
import org.qubic.qx.sync.domain.OrderBook;
import org.qubic.qx.sync.repository.OrderBookRepository;
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
     * Makes sure that there is at least one order book stored for every known asset. Needs the <b>current</b> tick number
     * as input so that we know what tick number to store a newly retrieved order book for.
     * @param currentTickNumber The current tick number. Order books that are not stored will be retrieved from the network.
     * @return Flux of order books for all assets that needed to be retrieved from the backend nodes.
     */
    public Flux<OrderBook> initializeOrderBooks(long currentTickNumber) {
        return Flux.fromIterable(assets.getAssets())
                .flatMap(asset -> orderBookRepository.hasOrderBook(asset.issuer(), asset.name())
                        .filter(found -> !found)
                        .flatMap(x -> retrieveAndStoreOrderBook(currentTickNumber, asset)));
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
                        .switchIfEmpty(Mono.defer(() -> retrieveAndStoreOrderBook(currentTickNumber, asset)))
                        .doOnNext(ob -> log.debug("[{}] order book: {}", ob.assetName(), ob)));
    }

    /**
     * Gets the latest stored order books before the specified tick number.
     * @param tickNumber The tick number the order book was created before.
     * @param assetSet The assets to load the order books for.
     * @return The order books that were found. It is not certain that there is one order book stored for every requested asset.
     */
    public Flux<OrderBook> loadLatestOrderBooksBeforeTick(long tickNumber, Set<Asset> assetSet) {
        return Flux.fromIterable(assetSet)
                .flatMap(asset -> orderBookRepository.getPreviousOrderBookBefore(asset.issuer(), asset.name(), tickNumber));
    }

    private Mono<OrderBook> retrieveAndStoreOrderBook(long currentTickNumber, Asset asset) {
        return retrieveOrderBook(currentTickNumber, asset)
                .flatMap(ob -> orderBookRepository.storeOrderBook(ob).map(x -> ob));
    }

    private Mono<OrderBook> retrieveOrderBook(long tickNumber, Asset asset) {
        Mono<List<AssetOrder>> asks = qxApiService.getAssetAskOrders(asset.issuer(), asset.name()).retry(3);
        Mono<List<AssetOrder>> bids = qxApiService.getAssetBidOrders(asset.issuer(), asset.name()).retry(3);
        return Mono.zip(asks, bids).map(t2 -> new OrderBook(tickNumber, asset.issuer(), asset.name(), t2.getT1(), t2.getT2()));
    }
}
