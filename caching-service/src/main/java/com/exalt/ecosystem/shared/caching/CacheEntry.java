package com.exalt.ecosystem.shared.caching;

import java.util.concurrent.TimeUnit;

/**
 * Represents a cache entry with value and metadata.
 *
 * @param <T> Type of the cached value
 */
public class CacheEntry<T> {
    private final T value;
    private final long expirationTime;
    private final long creationTime;
    
    /**
     * Creates a new cache entry with no expiration.
     *
     * @param value The value to cache
     */
    public CacheEntry(T value) {
        this(value, -1, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Creates a new cache entry with specified expiration.
     *
     * @param value    The value to cache
     * @param ttl      Time to live duration
     * @param timeUnit Time unit for the TTL
     */
    public CacheEntry(T value, long ttl, TimeUnit timeUnit) {
        this.value = value;
        this.creationTime = System.currentTimeMillis();
        this.expirationTime = ttl < 0 ? -1 : this.creationTime + timeUnit.toMillis(ttl);
    }
    
    /**
     * Gets the cached value.
     *
     * @return The cached value
     */
    public T getValue() {
        return value;
    }
    
    /**
     * Gets the expiration time in milliseconds since epoch.
     *
     * @return The expiration time or -1 if never expires
     */
    public long getExpirationTime() {
        return expirationTime;
    }
    
    /**
     * Gets the creation time in milliseconds since epoch.
     *
     * @return The creation time
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Checks if this cache entry has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expirationTime > 0 && System.currentTimeMillis() > expirationTime;
    }
}
