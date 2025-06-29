package com.exalt.ecosystem.shared.caching;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Core interface defining cache operations.
 * Implementations can provide different caching strategies (in-memory, Redis, etc.)
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cached value
 */
public interface Cache<K, V> {
    
    /**
     * Gets a value from the cache.
     *
     * @param key The cache key
     * @return The cached value, or null if not found or expired
     * @throws CacheException if an error occurs during the operation
     */
    V get(K key) throws CacheException;
    
    /**
     * Gets a value from the cache as a specific type.
     *
     * @param <T>   The expected type of the cached value
     * @param key   The cache key
     * @param clazz The class of the expected type
     * @return The cached value cast to the specified type, or null if not found or expired
     * @throws CacheException if an error occurs during the operation or the value cannot be cast
     */
    <T> T get(K key, Class<T> clazz) throws CacheException;
    
    /**
     * Gets a value from the cache or puts the value if not found.
     *
     * @param key      The cache key
     * @param supplier A supplier to provide the value if not found in cache
     * @return The cached value or the newly supplied value
     * @throws CacheException if an error occurs during the operation
     */
    V getOrPut(K key, Supplier<V> supplier) throws CacheException;
    
    /**
     * Gets a value from the cache or puts the value if not found, with a TTL.
     *
     * @param key      The cache key
     * @param supplier A supplier to provide the value if not found in cache
     * @param ttl      Time to live duration
     * @param timeUnit Time unit for the TTL
     * @return The cached value or the newly supplied value
     * @throws CacheException if an error occurs during the operation
     */
    V getOrPut(K key, Supplier<V> supplier, long ttl, TimeUnit timeUnit) throws CacheException;
    
    /**
     * Puts a value in the cache.
     *
     * @param key   The cache key
     * @param value The value to cache
     * @throws CacheException if an error occurs during the operation
     */
    void put(K key, V value) throws CacheException;
    
    /**
     * Puts a value in the cache with a TTL.
     *
     * @param key      The cache key
     * @param value    The value to cache
     * @param ttl      Time to live duration
     * @param timeUnit Time unit for the TTL
     * @throws CacheException if an error occurs during the operation
     */
    void put(K key, V value, long ttl, TimeUnit timeUnit) throws CacheException;
    
    /**
     * Checks if a key exists in the cache.
     *
     * @param key The cache key
     * @return true if the key exists and is not expired, false otherwise
     * @throws CacheException if an error occurs during the operation
     */
    boolean containsKey(K key) throws CacheException;
    
    /**
     * Removes a value from the cache.
     *
     * @param key The cache key
     * @return true if the key was found and removed, false otherwise
     * @throws CacheException if an error occurs during the operation
     */
    boolean remove(K key) throws CacheException;
    
    /**
     * Clears all entries from the cache.
     *
     * @throws CacheException if an error occurs during the operation
     */
    void clear() throws CacheException;
    
    /**
     * Gets the number of entries in the cache.
     *
     * @return The number of cache entries
     * @throws CacheException if an error occurs during the operation
     */
    long size() throws CacheException;
    
    /**
     * Gets the name of this cache.
     *
     * @return The cache name
     */
    String getName();
}
