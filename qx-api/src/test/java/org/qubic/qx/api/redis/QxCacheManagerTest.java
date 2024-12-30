package org.qubic.qx.api.redis;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;
import static org.qubic.qx.api.redis.QxCacheManager.*;

class QxCacheManagerTest {

    private final CacheManager redisCacheManager = mock();
    private final QxCacheManager cacheManager = new QxCacheManager(redisCacheManager);

    @Test
    void evictTradesCache() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_TRADES)).thenReturn(cache);
        cacheManager.evictTradesCache();
        verify(cache).clear();
    }

    @Test
    void evictTradeCacheForEntity() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ENTITY_TRADES)).thenReturn(cache);
        cacheManager.evictTradeCacheForEntity("foo");
        verify(cache).evict("foo");

    }

    @Test
    void evictTradeCacheForAsset() {
        Cache cache = mock();
        when(redisCacheManager.getCache(CACHE_NAME_ASSET_TRADES)).thenReturn(cache);
        cacheManager.evictTradeCacheForAsset("foo", "bar");
        verify(cache).evict("foo:bar");
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
        verify(cache, times(2)).evict("foo:bar");
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
        verify(cache).evict("foo:bar");
    }
}