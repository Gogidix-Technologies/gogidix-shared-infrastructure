package com.exalt.ecosystem.shared.caching;

import com.exalt.ecosystem.shared.caching.local.InMemoryCacheFactory;
import com.exalt.ecosystem.shared.caching.redis.RedisCacheFactory;

/**
 * Main entry point for the cache library.
 * Provides factory methods to create cache instances for different
 * cache implementations.
 */
public class CacheManager {
    
    /**
     * Creates a cache factory for in-memory caching.
     *
     * @return An in-memory cache factory
     */
    public static CacheFactory forInMemory() {
        return new InMemoryCacheFactory();
    }
    
    /**
     * Creates a cache factory for Redis caching.
     *
     * @return A Redis cache factory
     */
    public static CacheFactory forRedis() {
        return new RedisCacheFactory();
    }
    
    /**
     * Creates a cache factory for the specified cache type.
     *
     * @param cacheType The cache type to use
     * @return A cache factory for the specified type
     */
    public static CacheFactory forType(CacheType cacheType) {
        switch (cacheType) {
            case IN_MEMORY:
                return forInMemory();
            case REDIS:
                return forRedis();
            default:
                throw new IllegalArgumentException("Unsupported cache type: " + cacheType);
        }
    }
    
    /**
     * Creates a cache of the specified type with the given name and configuration.
     *
     * @param <K>       The type of cache keys
     * @param <V>       The type of cached values
     * @param cacheType The cache type to use
     * @param name      The name of the cache
     * @param config    The cache configuration
     * @return A new cache instance
     * @throws CacheException if an error occurs during cache creation
     */
    public static <K, V> Cache<K, V> createCache(CacheType cacheType, String name, CacheConfig config)
            throws CacheException {
        return forType(cacheType).createCache(name, config);
    }
    
    /**
     * Supported cache types.
     */
    public enum CacheType {
        IN_MEMORY,
        REDIS
    }
}
