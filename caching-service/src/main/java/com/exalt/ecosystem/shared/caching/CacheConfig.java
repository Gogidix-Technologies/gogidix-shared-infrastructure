package com.exalt.ecosystem.shared.caching;

/**
 * Configuration class for cache instances.
 * Contains settings for cache behavior such as expiration, eviction policy, etc.
 */
public class CacheConfig {
    
    // Common cache settings
    private int initialCapacity = 100;
    private int maxSize = 1000;
    private boolean recordStats = false;
    private long defaultTtlSeconds = -1; // -1 means no default expiration
    private EvictionPolicy evictionPolicy = EvictionPolicy.LRU;
    
    // Specific settings for different cache types
    private String connectionString;
    private int connectionTimeout = 2000;
    private String username;
    private String password;
    private boolean useSsl = false;
    
    /**
     * Supported cache eviction policies.
     */
    public enum EvictionPolicy {
        /** Least Recently Used - evicts least recently accessed entries first */
        LRU,
        /** Least Frequently Used - evicts least frequently accessed entries first */
        LFU,
        /** First In First Out - evicts oldest entries first */
        FIFO,
        /** Random Replacement - evicts random entries */
        RANDOM
    }
    
    /**
     * Default constructor with default settings.
     */
    public CacheConfig() {
    }
    
    /**
     * Creates a configuration with specified capacity and size limit.
     *
     * @param initialCapacity Initial cache capacity
     * @param maxSize         Maximum cache size
     */
    public CacheConfig(int initialCapacity, int maxSize) {
        this.initialCapacity = initialCapacity;
        this.maxSize = maxSize;
    }
    
    /**
     * Gets the initial capacity of the cache.
     *
     * @return The initial capacity
     */
    public int getInitialCapacity() {
        return initialCapacity;
    }
    
    /**
     * Sets the initial capacity of the cache.
     *
     * @param initialCapacity The initial capacity
     * @return This config instance for chaining
     */
    public CacheConfig setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        return this;
    }
    
    /**
     * Gets the maximum size of the cache.
     *
     * @return The maximum size
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * Sets the maximum size of the cache.
     *
     * @param maxSize The maximum size
     * @return This config instance for chaining
     */
    public CacheConfig setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }
    
    /**
     * Checks if statistics recording is enabled.
     *
     * @return true if statistics are enabled, false otherwise
     */
    public boolean isRecordStats() {
        return recordStats;
    }
    
    /**
     * Enables or disables statistics recording.
     *
     * @param recordStats true to enable statistics, false to disable
     * @return This config instance for chaining
     */
    public CacheConfig setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
        return this;
    }
    
    /**
     * Gets the default TTL in seconds.
     *
     * @return The default TTL or -1 if not set
     */
    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }
    
    /**
     * Sets the default TTL in seconds.
     *
     * @param defaultTtlSeconds The default TTL or -1 for no expiration
     * @return This config instance for chaining
     */
    public CacheConfig setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
        return this;
    }
    
    /**
     * Gets the eviction policy.
     *
     * @return The eviction policy
     */
    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }
    
    /**
     * Sets the eviction policy.
     *
     * @param evictionPolicy The eviction policy
     * @return This config instance for chaining
     */
    public CacheConfig setEvictionPolicy(EvictionPolicy evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
        return this;
    }
    
    /**
     * Gets the connection string for remote caches.
     *
     * @return The connection string
     */
    public String getConnectionString() {
        return connectionString;
    }
    
    /**
     * Sets the connection string for remote caches.
     *
     * @param connectionString The connection string
     * @return This config instance for chaining
     */
    public CacheConfig setConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }
    
    /**
     * Gets the connection timeout in milliseconds.
     *
     * @return The connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param connectionTimeout The connection timeout
     * @return This config instance for chaining
     */
    public CacheConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    
    /**
     * Gets the username for authenticated caches.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username for authenticated caches.
     *
     * @param username The username
     * @return This config instance for chaining
     */
    public CacheConfig setUsername(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * Gets the password for authenticated caches.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password for authenticated caches.
     *
     * @param password The password
     * @return This config instance for chaining
     */
    public CacheConfig setPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * Checks if SSL is enabled for remote connections.
     *
     * @return true if SSL is enabled, false otherwise
     */
    public boolean isUseSsl() {
        return useSsl;
    }
    
    /**
     * Enables or disables SSL for remote connections.
     *
     * @param useSsl true to enable SSL, false to disable
     * @return This config instance for chaining
     */
    public CacheConfig setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
        return this;
    }
}
