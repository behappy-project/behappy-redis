package org.xiaowu.behappy.redis.repository;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * @author 94391
 */
public record CacheManagerRepository(CacheManager cacheManager) {

    @SuppressWarnings({"unchecked"})
    public <T> T getCache(String cacheName,String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null) {
            return null;
        }
        return (T)valueWrapper.get();
    }

    public void putCache(String cacheName,String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public void evictCache(String cacheName,String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
