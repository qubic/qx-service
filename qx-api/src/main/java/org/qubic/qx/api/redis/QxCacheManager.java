package org.qubic.qx.api.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;

import java.util.Objects;

@Slf4j
public class QxCacheManager {

    private final CacheManager cacheManager;

    public static final String CACHE_NAME_FEES = "cache:fees";
    public static final String CACHE_NAME_ASSETS = "cache:assets";

    public static final String CACHE_NAME_TRADES = "cache:trades";
    public static final String CACHE_NAME_ASSET_TRADES = "cache:assetTrades";
    public static final String CACHE_NAME_ENTITY_TRADES = "cache:entityTrades";

    public static final String CACHE_NAME_ASSET_ASKS = "cache:assetAsks";
    public static final String CACHE_NAME_ASSET_BIDS = "cache:assetBids";
    public static final String CACHE_NAME_ENTITY_ASKS = "cache:entityAsks";
    public static final String CACHE_NAME_ENTITY_BIDS = "cache:entityBids";

    public static final String CACHE_NAME_ISSUED_ASSETS = "cache:issuedAssets";

    public static final String CACHE_NAME_TRANSFERS = "cache:transfers";
    public static final String CACHE_NAME_TRANSFERS_ASSET = "cache:transfersAsset";
    public static final String CACHE_NAME_TRANSFERS_ENTITY = "cache:transfersEntity";

    public static final String CACHE_NAME_ORDERS = "cache:orders";
    public static final String CACHE_NAME_ORDERS_ASSET = "cache:ordersAsset";
    public static final String CACHE_NAME_ORDERS_ENTITY = "cache:ordersEntity";

    public static final String CACHE_KEY_ASSET = "#issuer + ':' + #asset";

    public static final String CACHE_NAME_CHART_AVG_PRICE = "cache:chartAvgPrice";

    public QxCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictTradesCache() {
        log.debug("Clear general trades cache.");
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_TRADES)).clear();
    }

    public void evictTradeCacheForEntity(String identity) {
        log.debug("Evicting trade cache for entity [{}].", identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ENTITY_TRADES)).evict(identity);
    }

    public void evictTradeCacheForAsset(String issuer, String name) {
        log.debug("Evicting cache for asset with issuer [{}] and name [{}].", issuer, name);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSET_TRADES)).evict(String.format("%s:%s", issuer, name));
    }

    public void evictOrdersCache() {
        log.debug("Clear general oder transaction cache.");
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ORDERS)).clear();
    }

    public void evictOrderCacheForEntity(String identity) {
        log.debug("Evicting oder cache for entity [{}].", identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ENTITY_ASKS)).evict(identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ENTITY_BIDS)).evict(identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ORDERS_ENTITY)).evict(identity);
    }

    public void evictOrderCacheForAsset(String issuer, String name) {
        log.debug("Evicting oder cache for asset for issuer [{}] and name [{}].", issuer, name);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSET_ASKS)).evict(String.format("%s:%s", issuer, name));
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSET_BIDS)).evict(String.format("%s:%s", issuer, name));
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ORDERS_ASSET)).evict(String.format("%s:%s", issuer, name));
    }

    public void evictTransferCache() {
        log.debug("Clear general transfer transaction cache.");
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_TRANSFERS)).clear();
    }

    public void evictTransferCacheForEntity(String identity) {
        log.debug("Evicting transfer cache for entity [{}].", identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_TRANSFERS_ENTITY)).evict(identity);
    }

    public void evictTransferCacheForAsset(String issuer, String name) {
        log.debug("Evicting transfer cache for asset for issuer [{}] and name [{}].", issuer, name);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_TRANSFERS_ASSET)).evict(String.format("%s:%s", issuer, name));
    }

    public void evictChartCachesForAsset(String issuer, String name) {
        log.debug("Evicting chart caches for assets issuer [{}] and name [{}].", issuer, name);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_CHART_AVG_PRICE)).clear();
    }

    public void evictAssetsCaches() {
        log.debug("Evicting cache for issued assets.");
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSETS)).clear();
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ISSUED_ASSETS)).clear();
    }

}
