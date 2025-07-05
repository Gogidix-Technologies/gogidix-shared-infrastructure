package com.gogidix.ecosystem.shared.caching.local;

import com.gogidix.ecosystem.shared.caching.Cache;
import com.gogidix.ecosystem.shared.caching.CacheConfig;
import com.gogidix.ecosystem.shared.caching.CacheException;
import com.gogidix.ecosystem.shared.caching.CacheFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating in-memory cache instances.
 */
public class InMemoryCacheFactory implements CacheFactory {
    
    private final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    
    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> createCache(String name, CacheConfig config) throws CacheException {
        if (name == null || name.isEmpty()) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache name cannot be null or empty"
            );
        }
        
        if (config == null) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache configuration cannot be null"
            );
        }
        
        if (caches.containsKey(name)) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache with name '" + name + "' already exists"
            );
        }
        
        Cache<K, V> cache = new InMemoryCache<>(name, config);
        caches.put(name, cache);
        
        return cache;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config) throws CacheException {
        if (name == null || name.isEmpty()) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache name cannot be null or empty"
            );
        }
        
        if (config == null) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache configuration cannot be null"
            );
        }
        
        Cache<?, ?> cache = caches.get(name);
        
        if (cache == null) {
            return createCache(name, config);
        }
        
        return (Cache<K, V>) cache;
    }
    
    @Override
    public boolean removeCache(String name) throws CacheException {
        if (name == null || name.isEmpty()) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache name cannot be null or empty"
            );
        }
        
        Cache<?, ?> cache = caches.remove(name);
        
        if (cache != null) {
            try {
                cache.clear();
                return true;
            } catch (CacheException e) {
                throw new CacheException(
                    CacheException.ErrorCode.UNKNOWN_ERROR,
                    "Error clearing cache during removal: " + e.getMessage(),
                    e
                );
            }
        }
        
        return false;
    }
    
    @Override
    public boolean cacheExists(String name) throws CacheException {
        if (name == null || name.isEmpty()) {
            throw new CacheException(
                CacheException.ErrorCode.UNKNOWN_ERROR,
                "Cache name cannot be null or empty"
            );
        }
        
        return caches.containsKey(name);
    }
    
    @Override
    public void close() throws CacheException {
        for (Map.Entry<String, Cache<?, ?>> entry : caches.entrySet()) {
            try {
                entry.getValue().clear();
            } catch (CacheException e) {
                // Log the error but continue closing other caches
                System.err.println("Error clearing cache '" + entry.getKey() + "': " + e.getMessage());
            }
        }
        
        caches.clear();
    }
}
