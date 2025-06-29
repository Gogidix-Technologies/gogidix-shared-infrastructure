# Currency Exchange Service API Documentation

## Core Exchange Rate API

### ExchangeRateService
- `ExchangeRateService(config)`: Initialize exchange rate service with provider and cache configuration
- `ExchangeRateService(providers, cache, options)`: Initialize with specific providers and cache manager
- `getExchangeRate(baseCurrency, targetCurrency)`: Get current exchange rate between two currencies
- `getExchangeRate(baseCurrency, targetCurrency, options)`: Get exchange rate with specific options
- `getMultipleRates(baseCurrency, targetCurrencies)`: Get exchange rates from base to multiple target currencies
- `getAllRates()`: Get all available exchange rates from current base currency
- `getAllRates(baseCurrency)`: Get all rates from specified base currency
- `updateRate(currency, rateData)`: Update exchange rate for specific currency
- `updateMultipleRates(rateUpdates)`: Update multiple exchange rates in batch
- `getLastUpdate(currency)`: Get timestamp of last rate update for currency
- `getLastUpdate()`: Get timestamp of last system update
- `isRateStale(currency, threshold)`: Check if rate is older than specified threshold
- `refreshRate(currency)`: Force refresh of specific currency rate
- `refreshAllRates()`: Force refresh of all currency rates
- `getSupportedCurrencies()`: Get list of all supported currencies
- `addSupportedCurrency(currency, config)`: Add new supported currency
- `removeSupportedCurrency(currency)`: Remove currency from supported list
- `startScheduler()`: Start automatic rate update scheduler
- `stopScheduler()`: Stop automatic rate update scheduler
- `getSchedulerStatus()`: Get current scheduler status and next update time
- `setUpdateFrequency(frequency)`: Set rate update frequency
- `getProviderHealth()`: Get health status of all rate providers

### CurrencyConverter
- `CurrencyConverter(exchangeRateService)`: Initialize converter with exchange rate service
- `CurrencyConverter(exchangeRateService, config)`: Initialize with custom configuration
- `convert(amount, fromCurrency, toCurrency)`: Convert amount between currencies
- `convert(amount, fromCurrency, toCurrency, options)`: Convert with specific options
- `convertWithFees(amount, fromCurrency, toCurrency, feeConfig)`: Convert amount including fees
- `batchConvert(conversions)`: Convert multiple amounts in single operation
- `getConversionRate(fromCurrency, toCurrency)`: Get conversion rate including fees
- `getConversionRate(fromCurrency, toCurrency, includeSpread)`: Get rate with optional spread
- `calculateFees(amount, fromCurrency, toCurrency)`: Calculate conversion fees
- `calculateFees(amount, fromCurrency, toCurrency, feeStructure)`: Calculate with custom fee structure
- `validateConversion(amount, fromCurrency, toCurrency)`: Validate conversion parameters
- `getMinimumAmount(currency)`: Get minimum convertible amount for currency
- `getMaximumAmount(currency)`: Get maximum convertible amount for currency
- `roundToPrecision(amount, currency)`: Round amount to currency-specific precision
- `getPrecision(currency)`: Get decimal precision for currency
- `setSupportedPairs(pairs)`: Set supported currency pairs
- `getSupportedPairs()`: Get list of supported currency pairs
- `enableCaching(enabled)`: Enable or disable conversion result caching
- `clearCache()`: Clear conversion result cache

### ProviderManager
- `ProviderManager(providers)`: Initialize with list of rate providers
- `ProviderManager(providers, config)`: Initialize with providers and configuration
- `fetchRate(currency, providers)`: Fetch rate from specified providers
- `fetchRateWithFallback(currency)`: Fetch rate with automatic provider fallback
- `fetchAllRates(providers)`: Fetch all rates from specified providers
- `validateRate(rate, validationRules)`: Validate rate against business rules
- `aggregateRates(rates, aggregationMethod)`: Aggregate rates from multiple providers
- `getProviderStatus(providerName)`: Get operational status of specific provider
- `getAllProviderStatus()`: Get status of all configured providers
- `enableProvider(providerName)`: Enable specific rate provider
- `disableProvider(providerName)`: Disable specific rate provider
- `setProviderPriority(priorities)`: Set provider priority order
- `getProviderPriority()`: Get current provider priority configuration
- `addProvider(providerName, providerConfig)`: Add new rate provider
- `removeProvider(providerName)`: Remove rate provider
- `testProvider(providerName)`: Test connectivity and functionality of provider
- `getProviderMetrics(providerName)`: Get performance metrics for provider
- `setProviderTimeout(providerName, timeout)`: Set timeout for provider requests

## Cache Management API

### RedisCacheManager
- `RedisCacheManager(redisConfig)`: Initialize Redis cache manager with connection config
- `connect()`: Establish connection to Redis server
- `disconnect()`: Close connection to Redis server
- `isConnected()`: Check Redis connection status
- `get(key)`: Retrieve value from cache by key
- `get(key, defaultValue)`: Get value with default if key doesn't exist
- `set(key, value)`: Store value in cache with default TTL
- `set(key, value, ttl)`: Store value with specific TTL in seconds
- `setex(key, value, ttl)`: Set value with expiration time
- `delete(key)`: Remove key from cache
- `deletePattern(pattern)`: Remove all keys matching pattern
- `exists(key)`: Check if key exists in cache
- `exists(keys)`: Check existence of multiple keys
- `expire(key, ttl)`: Set expiration time for existing key
- `ttl(key)`: Get remaining TTL for key
- `clear()`: Remove all entries from cache
- `keys(pattern)`: Get all keys matching pattern
- `getStats()`: Get cache performance statistics
- `getMemoryUsage()`: Get Redis memory usage information
- `ping()`: Test Redis server connectivity
- `flushdb()`: Clear current database
- `info()`: Get Redis server information

### MemoryCacheManager
- `MemoryCacheManager(maxSize)`: Initialize in-memory cache with size limit
- `MemoryCacheManager(config)`: Initialize with detailed configuration
- `get(key)`: Get value from memory cache
- `set(key, value, ttl)`: Store value in memory cache
- `delete(key)`: Remove key from memory cache
- `clear()`: Clear all cache entries
- `size()`: Get current number of cached items
- `getStats()`: Get cache hit/miss statistics
- `getMemoryUsage()`: Get approximate memory usage
- `enableLRU(enabled)`: Enable/disable LRU eviction
- `setMaxSize(maxSize)`: Set maximum cache size
- `getKeys()`: Get all cached keys

### CacheInvalidationService
- `CacheInvalidationService(cacheManager)`: Initialize with cache manager
- `invalidateRate(currency)`: Invalidate cached rate for specific currency
- `invalidateAllRates()`: Invalidate all cached exchange rates
- `invalidatePattern(pattern)`: Invalidate all keys matching pattern
- `invalidateByTag(tag)`: Invalidate all entries with specific tag
- `scheduleInvalidation(key, delay)`: Schedule future cache invalidation
- `cancelScheduledInvalidation(key)`: Cancel scheduled invalidation
- `getInvalidationEvents()`: Get history of invalidation events
- `addInvalidationRule(rule)`: Add automatic invalidation rule
- `removeInvalidationRule(ruleId)`: Remove invalidation rule
- `getInvalidationRules()`: Get all configured invalidation rules

## Historical Data API

### HistoricalRateService
- `HistoricalRateService(storageConfig)`: Initialize with storage configuration
- `HistoricalRateService(storage, aggregator)`: Initialize with storage and aggregator
- `storeRate(currency, rateData)`: Store historical rate data
- `storeRate(currency, rateData, timestamp)`: Store rate with specific timestamp
- `storeBatchRates(rateDataArray)`: Store multiple rates in batch
- `getRates(currency, startDate, endDate)`: Get historical rates for date range
- `getRates(currency, startDate, endDate, interval)`: Get rates with specific interval
- `getLatestRate(currency)`: Get most recent historical rate
- `getStats(currency, period)`: Get statistical analysis for period
- `getStats(currency, startDate, endDate)`: Get stats for specific date range
- `getTrend(currency, period)`: Get trend analysis
- `getTrendAnalysis(currency, startDate, endDate)`: Get detailed trend analysis
- `getVolatility(currency, period)`: Calculate currency volatility
- `getCorrelation(currency1, currency2, period)`: Get correlation between currencies
- `aggregate(interval)`: Run data aggregation for interval
- `scheduleAggregation(interval, schedule)`: Schedule automatic aggregation
- `cleanup()`: Clean up old data according to retention policy
- `setRetentionPolicy(policy)`: Set data retention policy
- `getRetentionPolicy()`: Get current retention policy
- `exportData(currency, startDate, endDate, format)`: Export historical data

### DataAggregator
- `DataAggregator()`: Initialize data aggregator with default settings
- `DataAggregator(config)`: Initialize with custom aggregation configuration
- `aggregate(rawData, interval)`: Aggregate raw data to specified interval
- `aggregateToHourly(minuteData)`: Aggregate minute data to hourly
- `aggregateToDaily(hourlyData)`: Aggregate hourly data to daily
- `aggregateToWeekly(dailyData)`: Aggregate daily data to weekly
- `aggregateToMonthly(dailyData)`: Aggregate daily data to monthly
- `calculateOHLC(rates)`: Calculate Open, High, Low, Close from rates
- `calculateVolume(data)`: Calculate volume metrics
- `calculateAverage(rates, method)`: Calculate average using specified method
- `setAggregationRules(rules)`: Set custom aggregation rules
- `getAggregationRules()`: Get current aggregation rules

### TrendAnalyzer
- `TrendAnalyzer()`: Initialize trend analyzer with default parameters
- `TrendAnalyzer(config)`: Initialize with custom analysis configuration
- `analyzeTrend(rates)`: Analyze overall trend in rate data
- `analyzeTrend(rates, method)`: Analyze trend using specific method
- `calculateMovingAverage(rates, period)`: Calculate simple moving average
- `calculateEMA(rates, period)`: Calculate exponential moving average
- `calculateRSI(rates, period)`: Calculate Relative Strength Index
- `calculateMACD(rates)`: Calculate MACD indicator
- `calculateBollingerBands(rates, period)`: Calculate Bollinger Bands
- `calculateStochasticOscillator(rates, period)`: Calculate Stochastic Oscillator
- `detectSupportResistance(rates)`: Detect support and resistance levels
- `forecast(rates, periods)`: Generate rate forecast for future periods
- `forecast(rates, periods, method)`: Forecast using specific method
- `calculateConfidenceInterval(forecast, confidence)`: Calculate forecast confidence interval
- `getAnalysisResults()`: Get last analysis results
- `setAnalysisParameters(params)`: Set analysis parameters

## Provider Integration API

### ForexProvider
- `ForexProvider(apiConfig)`: Initialize Forex data provider
- `fetchRate(baseCurrency, targetCurrency)`: Fetch specific exchange rate
- `fetchAllRates(baseCurrency)`: Fetch all rates for base currency
- `fetchHistoricalRate(baseCurrency, targetCurrency, date)`: Fetch historical rate
- `getApiStatus()`: Get API service status
- `testConnection()`: Test API connectivity
- `setApiKey(apiKey)`: Set API authentication key
- `setRateLimit(limit)`: Set API rate limiting
- `getUsageStats()`: Get API usage statistics

### CentralBankProvider
- `CentralBankProvider(bankConfig)`: Initialize central bank provider
- `fetchOfficialRates(bank)`: Fetch official rates from central bank
- `fetchECBRates()`: Fetch European Central Bank rates
- `fetchFedRates()`: Fetch Federal Reserve rates
- `fetchBOJRates()`: Fetch Bank of Japan rates
- `fetchBOERates()`: Fetch Bank of England rates
- `getPublicationSchedule(bank)`: Get rate publication schedule
- `isMarketOpen(bank)`: Check if market is currently open

### CryptoProvider
- `CryptoProvider(exchangeConfig)`: Initialize cryptocurrency provider
- `fetchCryptoRate(cryptoCurrency, fiatCurrency)`: Fetch crypto to fiat rate
- `fetchAllCryptoRates(fiatCurrency)`: Fetch all crypto rates
- `fetchExchangeRates(exchange)`: Fetch rates from specific exchange
- `getMarketData(cryptocurrency)`: Get comprehensive market data
- `getOrderBook(pair, exchange)`: Get order book data
- `getTradingVolume(cryptocurrency, period)`: Get trading volume

### MockProvider
- `MockProvider()`: Initialize mock provider for testing
- `MockProvider(mockData)`: Initialize with predefined mock data
- `setMockRate(baseCurrency, targetCurrency, rate)`: Set mock exchange rate
- `setMockVolatility(currency, volatility)`: Set mock volatility
- `simulateDelay(delay)`: Simulate API response delay
- `simulateFailure(failureRate)`: Simulate API failures
- `generateRandomRates(currencies, baseRate, volatility)`: Generate random test rates

## REST API Endpoints

### Exchange Rate Endpoints
- `GET /api/v1/rates`: Get all current exchange rates
- `GET /api/v1/rates/{baseCurrency}`: Get all rates for specific base currency
- `GET /api/v1/rates/{baseCurrency}/{targetCurrency}`: Get specific exchange rate
- `POST /api/v1/rates/multiple`: Get multiple exchange rates in single request
- `GET /api/v1/rates/supported`: Get list of supported currencies
- `GET /api/v1/rates/last-updated`: Get last update timestamps for all rates
- `POST /api/v1/rates/refresh`: Force refresh of exchange rates
- `POST /api/v1/rates/refresh/{currency}`: Force refresh of specific currency

### Currency Conversion Endpoints
- `POST /api/v1/convert`: Convert amount between currencies
- `POST /api/v1/convert/batch`: Perform batch currency conversions
- `GET /api/v1/convert/rate/{fromCurrency}/{toCurrency}`: Get conversion rate
- `POST /api/v1/convert/fees`: Calculate conversion fees
- `GET /api/v1/convert/limits/{currency}`: Get conversion limits for currency
- `GET /api/v1/convert/precision/{currency}`: Get precision for currency

### Historical Data Endpoints
- `GET /api/v1/historical/{currency}`: Get historical rates for currency
- `GET /api/v1/historical/{currency}/stats`: Get statistical analysis
- `GET /api/v1/historical/{currency}/trend`: Get trend analysis
- `GET /api/v1/historical/{currency}/volatility`: Get volatility metrics
- `GET /api/v1/historical/{currency}/forecast`: Get rate forecast
- `POST /api/v1/historical/correlation`: Get correlation between currencies
- `GET /api/v1/historical/{currency}/export`: Export historical data

### Provider Management Endpoints
- `GET /api/v1/providers`: Get all configured providers
- `GET /api/v1/providers/{providerId}/status`: Get provider status
- `POST /api/v1/providers/{providerId}/test`: Test provider connectivity
- `PUT /api/v1/providers/{providerId}/enable`: Enable provider
- `PUT /api/v1/providers/{providerId}/disable`: Disable provider
- `GET /api/v1/providers/{providerId}/metrics`: Get provider metrics

### Cache Management Endpoints
- `GET /api/v1/cache/stats`: Get cache statistics
- `POST /api/v1/cache/clear`: Clear all cache
- `POST /api/v1/cache/clear/{pattern}`: Clear cache by pattern
- `GET /api/v1/cache/keys`: Get all cache keys
- `GET /api/v1/cache/health`: Get cache health status

### Health and Monitoring Endpoints
- `GET /health`: Service health status
- `GET /health/live`: Liveness probe for Kubernetes
- `GET /health/ready`: Readiness probe for Kubernetes
- `GET /health/detailed`: Detailed health information
- `GET /metrics`: Prometheus metrics endpoint
- `GET /api/v1/status`: Service status and uptime
- `GET /api/v1/info`: Service information and version

## WebSocket API

### Real-time Rate Updates
- `ws://host:port/ws/rates`: Real-time exchange rate updates
- `ws://host:port/ws/rates/{currency}`: Real-time updates for specific currency
- `ws://host:port/ws/conversions`: Real-time conversion rate updates

### WebSocket Message Types
- `rate_update`: Exchange rate update notification
- `rate_alert`: Alert for significant rate changes
- `provider_status`: Provider status change notification
- `system_alert`: System-wide alerts and notifications

## Event System API

### EventEmitter
- `EventEmitter()`: Initialize event emitter
- `on(event, listener)`: Add event listener
- `off(event, listener)`: Remove event listener
- `emit(event, data)`: Emit event with data
- `once(event, listener)`: Add one-time event listener
- `removeAllListeners(event)`: Remove all listeners for event

### Event Types
- `rate_updated`: Fired when exchange rate is updated
- `rate_fetch_failed`: Fired when rate fetch fails
- `cache_hit`: Fired when cache hit occurs
- `cache_miss`: Fired when cache miss occurs
- `provider_error`: Fired when provider error occurs
- `conversion_completed`: Fired when conversion is completed

## Error Handling

### CurrencyExchangeError
- `CurrencyExchangeError(message)`: Create generic currency exchange error
- `CurrencyExchangeError(message, code)`: Create error with specific code
- `getErrorCode()`: Get error code
- `getErrorDetails()`: Get detailed error information

### RateNotFoundError
- `RateNotFoundError(baseCurrency, targetCurrency)`: Create rate not found error
- `getBaseCurrency()`: Get base currency from error
- `getTargetCurrency()`: Get target currency from error

### ProviderError
- `ProviderError(providerName, message)`: Create provider-specific error
- `ProviderError(providerName, message, statusCode)`: Create with HTTP status
- `getProviderName()`: Get provider name from error
- `getStatusCode()`: Get HTTP status code

### ConversionError
- `ConversionError(message, amount, fromCurrency, toCurrency)`: Create conversion error
- `getAmount()`: Get amount from failed conversion
- `getFromCurrency()`: Get source currency
- `getToCurrency()`: Get target currency

### CacheError
- `CacheError(operation, key, message)`: Create cache operation error
- `getOperation()`: Get failed cache operation
- `getKey()`: Get cache key that caused error

### ValidationError
- `ValidationError(field, value, message)`: Create validation error
- `getField()`: Get field that failed validation
- `getValue()`: Get invalid value
- `getValidationRule()`: Get violated validation rule