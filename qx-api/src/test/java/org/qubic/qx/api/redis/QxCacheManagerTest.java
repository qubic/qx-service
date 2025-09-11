package org.qubic.qx.api.redis;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;

import static org.mockito.Mockito.*;
import static org.qubic.qx.api.redis.QxCacheManager.*;

class QxCacheManagerTest {

    private final RedisCacheManager redisCacheManager = mock();
    private final QxCacheManager cacheManager = new QxCacheManager(redisCacheManager);

    @Test
    void evictTradesCache() {
        Cache cache = mock();
        when(redisCacheManager.getCache(anyString())).thenReturn(mock());
        when(redisCacheManager.getCache(CACHE_NAME_TRADES)).thenReturn(cache);
        cacheManager.evictTradesCache("ISSUER");
        verify(cache).clear();
    }

    @Test
    void evictSmartContractTradesCache() {
        Cache cache = mock();
        when(redisCacheManager.getCache(anyString())).thenReturn(mock());
        when(redisCacheManager.getCache(CACHE_NAME_TRADES_SMART_CONTRACTS)).thenReturn(cache);
        cacheManager.evictTradesCache(Asset.SMART_CONTRACT_ISSUER);
        verify(cache).clear();
    }

    @Test
    void evictTokenTradesCache() {
        Cache cache = mock();
        when(redisCacheManager.getCache(anyString())).thenReturn(mock());
        when(redisCacheManager.getCache(CACHE_NAME_TRADES_TOKENS)).thenReturn(cache);
        cacheManager.evictTradesCache("ISSUER");
        verify(cache).clear();
    }

    @Test
    void evictTradeCacheForEntity() {
        RedisCache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ENTITY_TRADES)).thenReturn(cache);
        cacheManager.evictTradeCacheForEntity("foo");
        verify(cache).clear("SimpleKey \\[foo*");
    }

    @Test
    void evictTradeCacheForAsset() {
        RedisCache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ASSET_TRADES)).thenReturn(cache);
        cacheManager.evictTradeCacheForAsset("foo", "bar");
        verify(cache, times(1)).clear("SimpleKey \\[foo, bar*");
    }

    @Test
    void evictOrderCacheForEntity() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ENTITY_BIDS)).thenReturn(cache);
        when(redisCacheManager.getCache(CACHE_NAME_ENTITY_ASKS)).thenReturn(cache);
        cacheManager.evictOrderCacheForEntity("foo");
        verify(cache, times(2)).evict("foo");
    }

    @Test
    void evictOrderCacheForAsset() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ASSET_BIDS)).thenReturn(cache);
        when(redisCacheManager.getCache(CACHE_NAME_ASSET_ASKS)).thenReturn(cache);
        cacheManager.evictOrderCacheForAsset("foo", "bar");
        verify(cache, times(2)).evict("foo:bar:true");
        verify(cache, times(2)).evict("foo:bar:false");
    }

    @Test
    void evictTransferCache() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_TRANSFERS)).thenReturn(cache);
        cacheManager.evictTransferCache();
        verify(cache).clear();
    }

    @Test
    void evictTransferCacheForEntity() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_TRANSFERS_ENTITY)).thenReturn(cache);
        cacheManager.evictTransferCacheForEntity("foo");
        verify(cache).evict("foo");
    }

    @Test
    void evictTransferCacheForAsset() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_TRANSFERS_ASSET)).thenReturn(cache);
        cacheManager.evictTransferCacheForAsset("foo", "bar");
        verify(cache, times(1)).evict("foo:bar");
    }

    @Test
    void evictAssetsCache() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ASSETS)).thenReturn(cache);
        when(redisCacheManager.getCache(CACHE_NAME_ASSETS_VERIFIED)).thenReturn(cache);
        when(redisCacheManager.getCache(CACHE_NAME_ISSUED_ASSETS)).thenReturn(cache);
        cacheManager.evictAssetsCaches();
        verify(cache, times(3)).clear();
    }

}