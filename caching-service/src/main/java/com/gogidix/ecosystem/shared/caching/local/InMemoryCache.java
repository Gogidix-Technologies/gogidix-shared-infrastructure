package com.gogidix.ecosystem.shared.caching.local;

import com.gogidix.ecosystem.shared.caching.Cache;
import com.gogidix.ecosystem.shared.caching.CacheConfig;
import com.gogidix.ecosystem.shared.caching.CacheException;
import com.gogidix.ecosystem.shared.caching.CacheException.ErrorCode;
import com.gogidix.ecosystem.shared.caching.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * In-memory implementation of the Cache interface.
 * Stores cache entries in memory using a ConcurrentHashMap.
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cached value
 */
public class InMemoryCache<K, V> implements Cache<K, V> {
    private final String name;
    private final CacheConfig config;
    private final Map<K, CacheEntry<V>> cache;
    
    /**
     * Creates a new in-memory cache with the specified name and configuration.
     *
     * @param name   The name of the cache
     * @param config The cache configuration
     */
    public InMemoryCache(String name, CacheConfig config) {
        this.name = name;
        this.config = config;
        this.cache = new ConcurrentHashMap<>(config.getInitialCapacity());
    }
    
    @Override
    public V get(K key) throws CacheException {
        if (key == null) {
            throw new CacheException(ErrorCode.KEY_NOT_FOUND, "Cache key cannot be null");
        }
        
        CacheEntry<V> entry = cache.get(key);
        
        if (entry == null) {
            return null;
        }
        
        // Check if entry has expired
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return entry.getValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(K key, Class<T> clazz) throws CacheException {
        V value = get(key);
        
        if (value == null) {
            return null;
        }
        
        if (clazz.isInstance(value)) {
            return (T) value;
        } else {
            throw new CacheException(
                ErrorCode.TYPE_MISMATCH,
                "Cached value is not of the expected type. Expected: " + clazz.getName() + 
                ", Actual: " + value.getClass().getName()
            );
        }
    }
    
    @Override
    public V getOrPut(K key, Supplier<V> supplier) throws CacheException {
        V value = get(key);
        
        if (value == null) {
            value = supplier.get();
            put(key, value);
        }
        
        return value;
    }
    
    @Override
    public V getOrPut(K key, Supplier<V> supplier, long ttl, TimeUnit timeUnit) throws CacheException {
        V value = get(key);
        
        if (value == null) {
            value = supplier.get();
            put(key, value, ttl, timeUnit);
        }
        
        return value;
    }
    
    @Override
    public void put(K key, V value) throws CacheException {
        if (key == null) {
            throw new CacheException(ErrorCode.KEY_NOT_FOUND, "Cache key cannot be null");
        }
        
        if (value == null) {
            throw new CacheException(ErrorCode.UNKNOWN_ERROR, "Cache value cannot be null");
        }
        
        // Check if max size is reached
        evictIfNeeded();
        
        // Use default TTL if specified in config
        long ttl = config.getDefaultTtlSeconds();
        if (ttl > 0) {
            put(key, value, ttl, TimeUnit.SECONDS);
        } else {
            cache.put(key, new CacheEntry<>(value));
        }
    }
    
    @Override
    public void put(K key, V value, long ttl, TimeUnit timeUnit) throws CacheException {
        if (key == null) {
            throw new CacheException(ErrorCode.KEY_NOT_FOUND, "Cache key cannot be null");
        }
        
        if (value == null) {
            throw new CacheException(ErrorCode.UNKNOWN_ERROR, "Cache value cannot be null");
        }
        
        if (ttl <= 0) {
            throw new CacheException(ErrorCode.UNKNOWN_ERROR, "TTL must be positive");
        }
        
        // Check if max size is reached
        evictIfNeeded();
        
        cache.put(key, new CacheEntry<>(value, ttl, timeUnit));
    }
    
    @Override
    public boolean containsKey(K key) throws CacheException {
        if (key == null) {
            throw new CacheException(ErrorCode.KEY_NOT_FOUND, "Cache key cannot be null");
        }
        
        CacheEntry<V> entry = cache.get(key);
        
        if (entry == null) {
            return false;
        }
        
        // Check if entry has expired
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean remove(K key) throws CacheException {
        if (key == null) {
            throw new CacheException(ErrorCode.KEY_NOT_FOUND, "Cache key cannot be null");
        }
        
        return cache.remove(key) != null;
    }
    
    @Override
    public void clear() throws CacheException {
        cache.clear();
    }
    
    @Override
    public long size() throws CacheException {
        // Remove expired entries first to get an accurate count
        removeExpiredEntries();
        return cache.size();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Removes all expired entries from the cache.
     */
    private void removeExpiredEntries() {
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
            }
        }
    }
    
    /**
     * Evicts entries if the cache size exceeds the maximum size.
     */
    private void evictIfNeeded() {
        // First, remove expired entries
        removeExpiredEntries();
        
        if (cache.size() >= config.getMaxSize()) {
            // If still too large, apply eviction policy
            switch (config.getEvictionPolicy()) {
                case LRU:
                    evictLRU();
                    break;
                case LFU:
                    evictLFU();
                    break;
                case FIFO:
                    evictFIFO();
                    break;
                case RANDOM:
                    evictRandom();
                    break;
                default:
                    evictRandom(); // Default to random if unknown policy
            }
        }
    }
    
    /**
     * Evicts the least recently used entry.
     * Note: This is a simplified implementation. A real LRU cache would 
     * track access times for all entries.
     */
    private void evictLRU() {
        // For this simplified implementation, just remove the first entry
        // A real LRU would use a LinkedHashMap or similar structure
        if (!cache.isEmpty()) {
            K keyToRemove = cache.keySet().iterator().next();
            cache.remove(keyToRemove);
        }
    }
    
    /**
     * Evicts the least frequently used entry.
     * Note: This is a simplified implementation. A real LFU cache would 
     * track access counts for all entries.
     */
    private void evictLFU() {
        // For this simplified implementation, just remove the first entry
        // A real LFU would track access counts
        if (!cache.isEmpty()) {
            K keyToRemove = cache.keySet().iterator().next();
            cache.remove(keyToRemove);
        }
    }
    
    /**
     * Evicts the oldest entry (first in, first out).
     */
    private void evictFIFO() {
        // Similar to LRU in this simplified implementation
        if (!cache.isEmpty()) {
            K keyToRemove = cache.keySet().iterator().next();
            cache.remove(keyToRemove);
        }
    }
    
    /**
     * Evicts a random entry.
     */
    private void evictRandom() {
        if (!cache.isEmpty()) {
            int randomIndex = (int) (Math.random() * cache.size());
            K keyToRemove = null;
            int i = 0;
            
            for (K key : cache.keySet()) {
                if (i == randomIndex) {
                    keyToRemove = key;
                    break;
                }
                i++;
            }
            
            if (keyToRemove != null) {
                cache.remove(keyToRemove);
            }
        }
    }
}
