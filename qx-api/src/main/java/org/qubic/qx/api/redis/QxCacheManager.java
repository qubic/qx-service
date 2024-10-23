package org.qubic.qx.api.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;

import java.util.Objects;

@Slf4j
public class QxCacheManager {

    private final CacheManager cacheManager;

    public static final String CACHE_NAME_FEES = "fees";
    public static final String CACHE_NAME_ASSETS = "assets";
    public static final String CACHE_NAME_TRADES = "trades";
    public static final String CACHE_NAME_ASSET_TRADES = "assetTrades";
    public static final String CACHE_NAME_ENTITY_TRADES = "entityTrades";
    public static final String CACHE_NAME_ASSET_ASK_ORDERS = "assetAskOrders";
    public static final String CACHE_NAME_ASSET_BID_ORDERS = "assetBidOrders";
    public static final String CACHE_NAME_ENTITY_ASK_ORDERS = "entityAskOrders";
    public static final String CACHE_NAME_ENTITY_BID_ORDERS = "entityBidOrders";

    public static final String CACHE_KEY_ASSET = "#issuer + ':' + #asset";

    public QxCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictTrades() {
        log.debug("Evict trades cache.");
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_TRADES)).clear();
    }

    public void evictTradeCacheForEntity(String identity) {
        log.debug("Evicting trade cache for entity [{}].", identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ENTITY_TRADES)).evict(identity);
    }

    public void evictTradeCacheForAsset(String issuer, String name) {
        log.debug("Evicting caches for asset with issuer [{}] and name [{}].", issuer, name);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSET_TRADES)).evict(String.format("%s:%s", issuer, name));
    }

    public void evictOrderCachesForEntity(String identity) {
        log.debug("Evicting oder caches for entity [{}].", identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ENTITY_ASK_ORDERS)).evict(identity);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ENTITY_BID_ORDERS)).evict(identity);
    }

    public void evictOrderCachesForAsset(String issuer, String name) {
        log.debug("Evicting oder caches for asset for issuer [{}] and name [{}].", issuer, name);
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSET_ASK_ORDERS)).evict(String.format("%s:%s", issuer, name));
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSET_BID_ORDERS)).evict(String.format("%s:%s", issuer, name));
    }

}
