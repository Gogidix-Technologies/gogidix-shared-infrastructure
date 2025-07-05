package com.gogidix.ecosystem.shared.caching;

/**
 * Factory interface for creating cache instances.
 * Implementations can provide different caching strategies (in-memory, Redis, etc.)
 */
public interface CacheFactory {
    
    /**
     * Creates a cache with the specified name and configuration.
     *
     * @param <K>     The type of cache keys
     * @param <V>     The type of cached values
     * @param name    The name of the cache
     * @param config  The cache configuration
     * @return A new cache instance
     * @throws CacheException if an error occurs during cache creation
     */
    <K, V> Cache<K, V> createCache(String name, CacheConfig config) throws CacheException;
    
    /**
     * Gets an existing cache by name, or creates it if it doesn't exist.
     *
     * @param <K>     The type of cache keys
     * @param <V>     The type of cached values
     * @param name    The name of the cache
     * @param config  The cache configuration to use if creating a new cache
     * @return The existing or new cache instance
     * @throws CacheException if an error occurs during cache retrieval or creation
     */
    <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config) throws CacheException;
    
    /**
     * Removes a cache by name.
     *
     * @param name The name of the cache to remove
     * @return true if the cache was found and removed, false otherwise
     * @throws CacheException if an error occurs during the operation
     */
    boolean removeCache(String name) throws CacheException;
    
    /**
     * Checks if a cache with the specified name exists.
     *
     * @param name The name of the cache
     * @return true if the cache exists, false otherwise
     * @throws CacheException if an error occurs during the operation
     */
    boolean cacheExists(String name) throws CacheException;
    
    /**
     * Closes all caches created by this factory and releases resources.
     *
     * @throws CacheException if an error occurs during the operation
     */
    void close() throws CacheException;
}
