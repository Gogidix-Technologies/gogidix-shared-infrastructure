# Currency Exchange Service Documentation

## Overview

The Currency Exchange Service is a high-performance Node.js-based microservice that provides real-time currency exchange rates, currency conversion, and foreign exchange management capabilities for the Social E-commerce Ecosystem. It integrates with multiple external currency data providers, implements advanced caching strategies, and provides comprehensive rate management for global e-commerce operations.

## Components

### Core Components
- **ExchangeRateService**: Main service for fetching and managing exchange rates
- **CurrencyConverter**: Currency conversion calculations and operations
- **RateCache**: High-performance caching for exchange rates
- **ProviderManager**: Multiple currency data provider management
- **HistoricalRateService**: Historical exchange rate data management

### Data Providers
- **ForexProvider**: Forex market data integration
- **CentralBankProvider**: Central bank official rates
- **CryptoProvider**: Cryptocurrency exchange rates
- **CompositeProvider**: Multi-provider aggregation and validation
- **MockProvider**: Testing and development data provider

### Cache Management
- **RedisCacheManager**: Redis-based distributed caching
- **MemoryCacheManager**: In-memory caching for high-frequency data
- **CacheInvalidationService**: Smart cache invalidation strategies
- **CacheWarmupService**: Proactive cache population
- **CacheMetricsCollector**: Cache performance monitoring

### API Components
- **ExchangeRateController**: REST API endpoints for exchange rates
- **ConversionController**: Currency conversion API endpoints
- **HistoricalController**: Historical data API endpoints
- **HealthController**: Service health and monitoring endpoints
- **ConfigController**: Dynamic configuration management

## Getting Started

To use the Currency Exchange Service, follow these steps:

1. Configure the service with currency data providers
2. Set up caching and performance optimization
3. Configure supported currencies and conversion rules
4. Set up real-time rate updates
5. Enable monitoring and alerting

## Examples

### Basic Currency Exchange Service Setup

```javascript
import { ExchangeRateService } from '@exalt/currency-exchange-service';
import { ForexProvider, CentralBankProvider } from '@exalt/currency-providers';
import { RedisCacheManager } from '@exalt/cache-manager';
import { ExchangeRateController } from '@exalt/controllers';

class CurrencyExchangeServiceApplication {
    constructor() {
        this.app = express();
        this.setupProviders();
        this.setupCache();
        this.setupServices();
        this.setupRoutes();
        this.setupMiddleware();
    }
    
    setupProviders() {
        this.providers = {
            forex: new ForexProvider({
                apiKey: process.env.FOREX_API_KEY,
                baseUrl: 'https://api.forex.com/v1',
                timeout: 5000,
                retryAttempts: 3
            }),
            
            centralBank: new CentralBankProvider({
                sources: ['ECB', 'FED', 'BOJ', 'BOE'],
                updateFrequency: '0 9 * * MON-FRI', // 9 AM weekdays
                timeout: 10000
            }),
            
            crypto: new CryptoProvider({
                apiKey: process.env.CRYPTO_API_KEY,
                exchanges: ['coinbase', 'binance', 'kraken'],
                updateInterval: 60000 // 1 minute
            })
        };
        
        this.providerManager = new ProviderManager({
            providers: this.providers,
            fallbackStrategy: 'cascade',
            validationRules: {
                maxDeviationPercent: 5,
                minimumSources: 2,
                staleDataThreshold: 300000 // 5 minutes
            }
        });
    }
    
    setupCache() {
        this.cacheManager = new RedisCacheManager({
            host: process.env.REDIS_HOST || 'localhost',
            port: process.env.REDIS_PORT || 6379,
            password: process.env.REDIS_PASSWORD,
            keyPrefix: 'currency:',
            defaultTtl: 300, // 5 minutes
            
            cacheStrategies: {
                rates: {
                    ttl: 300, // 5 minutes for exchange rates
                    maxSize: 10000,
                    compressionEnabled: true
                },
                historical: {
                    ttl: 86400, // 24 hours for historical data
                    maxSize: 100000,
                    compressionEnabled: true
                },
                conversions: {
                    ttl: 60, // 1 minute for conversion results
                    maxSize: 50000,
                    compressionEnabled: false
                }
            }
        });
    }
    
    setupServices() {
        this.exchangeRateService = new ExchangeRateService({
            providerManager: this.providerManager,
            cacheManager: this.cacheManager,
            supportedCurrencies: [
                'USD', 'EUR', 'GBP', 'JPY', 'CAD', 'AUD', 'CHF', 'CNY',
                'KRW', 'SGD', 'HKD', 'INR', 'BRL', 'MXN', 'RUB', 'ZAR'
            ],
            baseCurrency: 'USD',
            updateSchedule: '*/5 * * * *', // Every 5 minutes
            enableRealTimeUpdates: true
        });
        
        this.currencyConverter = new CurrencyConverter({
            exchangeRateService: this.exchangeRateService,
            precisionDigits: 6,
            roundingMode: 'HALF_UP',
            enableCaching: true,
            cacheManager: this.cacheManager
        });
        
        this.historicalRateService = new HistoricalRateService({
            dataRetentionDays: 365,
            aggregationLevels: ['daily', 'weekly', 'monthly'],
            storageBackend: 'mongodb',
            connectionString: process.env.MONGODB_URL
        });
    }
    
    setupRoutes() {
        // Exchange rate endpoints
        this.app.use('/api/v1/rates', new ExchangeRateController({
            exchangeRateService: this.exchangeRateService,
            cacheManager: this.cacheManager,
            rateLimiter: {
                windowMs: 60000, // 1 minute
                maxRequests: 1000
            }
        }).getRouter());
        
        // Currency conversion endpoints
        this.app.use('/api/v1/convert', new ConversionController({
            currencyConverter: this.currencyConverter,
            rateLimiter: {
                windowMs: 60000,
                maxRequests: 500
            }
        }).getRouter());
        
        // Historical data endpoints
        this.app.use('/api/v1/historical', new HistoricalController({
            historicalRateService: this.historicalRateService,
            rateLimiter: {
                windowMs: 60000,
                maxRequests: 100
            }
        }).getRouter());
        
        // Health and monitoring
        this.app.use('/health', new HealthController({
            services: [this.exchangeRateService, this.cacheManager],
            providers: this.providers
        }).getRouter());
    }
    
    async start() {
        try {
            // Initialize cache
            await this.cacheManager.connect();
            
            // Start rate update scheduler
            await this.exchangeRateService.startScheduler();
            
            // Warm up cache with popular currency pairs
            await this.warmupCache();
            
            // Start HTTP server
            const port = process.env.PORT || 3402;
            this.server = this.app.listen(port, () => {
                console.log(`Currency Exchange Service listening on port ${port}`);
            });
            
        } catch (error) {
            console.error('Failed to start Currency Exchange Service:', error);
            process.exit(1);
        }
    }
    
    async warmupCache() {
        const popularPairs = [
            ['USD', 'EUR'], ['USD', 'GBP'], ['USD', 'JPY'], ['USD', 'CAD'],
            ['EUR', 'GBP'], ['EUR', 'JPY'], ['GBP', 'JPY'], ['USD', 'CNY']
        ];
        
        for (const [base, target] of popularPairs) {
            try {
                await this.exchangeRateService.getExchangeRate(base, target);
            } catch (error) {
                console.warn(`Failed to warm up cache for ${base}/${target}:`, error.message);
            }
        }
    }
}

// Start the service
const service = new CurrencyExchangeServiceApplication();
service.start();
```

### Exchange Rate Management

```javascript
import { ExchangeRateService, RateUpdateStrategy } from '@exalt/currency-exchange-service';

class ExchangeRateManager {
    constructor(exchangeRateService, providerManager) {
        this.exchangeRateService = exchangeRateService;
        this.providerManager = providerManager;
        this.setupUpdateStrategies();
    }
    
    setupUpdateStrategies() {
        // Real-time updates for major currencies
        this.majorCurrencyStrategy = new RateUpdateStrategy({
            currencies: ['USD', 'EUR', 'GBP', 'JPY'],
            updateFrequency: 60000, // 1 minute
            providers: ['forex', 'centralBank'],
            validationEnabled: true,
            alertOnLargeChanges: true,
            changeThreshold: 2.0 // 2% change triggers alert
        });
        
        // Less frequent updates for minor currencies
        this.minorCurrencyStrategy = new RateUpdateStrategy({
            currencies: ['ZAR', 'MXN', 'INR', 'BRL'],
            updateFrequency: 300000, // 5 minutes
            providers: ['centralBank'],
            validationEnabled: true,
            alertOnLargeChanges: false
        });
        
        // Cryptocurrency updates
        this.cryptoStrategy = new RateUpdateStrategy({
            currencies: ['BTC', 'ETH', 'LTC', 'XRP'],
            updateFrequency: 30000, // 30 seconds
            providers: ['crypto'],
            validationEnabled: true,
            alertOnLargeChanges: true,
            changeThreshold: 5.0 // 5% change threshold for crypto
        });
    }
    
    async getCurrentRate(baseCurrency, targetCurrency) {
        try {
            const rate = await this.exchangeRateService.getExchangeRate(baseCurrency, targetCurrency);
            
            if (!rate) {
                throw new Error(`Exchange rate not available for ${baseCurrency}/${targetCurrency}`);
            }
            
            return {
                baseCurrency,
                targetCurrency,
                rate: rate.value,
                timestamp: rate.timestamp,
                source: rate.source,
                spread: rate.spread,
                confidence: rate.confidence
            };
            
        } catch (error) {
            console.error(`Failed to get exchange rate for ${baseCurrency}/${targetCurrency}:`, error);
            throw error;
        }
    }
    
    async getMultipleRates(baseCurrency, targetCurrencies) {
        const ratePromises = targetCurrencies.map(target => 
            this.getCurrentRate(baseCurrency, target)
                .catch(error => ({ error: error.message, baseCurrency, targetCurrency: target }))
        );
        
        const results = await Promise.all(ratePromises);
        
        return {
            baseCurrency,
            rates: results.filter(result => !result.error),
            errors: results.filter(result => result.error)
        };
    }
    
    async updateRates(strategy) {
        const startTime = Date.now();
        const results = {
            updated: [],
            failed: [],
            unchanged: []
        };
        
        try {
            for (const currency of strategy.currencies) {
                try {
                    const updatedRate = await this.providerManager.fetchRate(currency, strategy.providers);
                    
                    if (updatedRate) {
                        const existingRate = await this.exchangeRateService.getExchangeRate('USD', currency);
                        
                        if (this.shouldUpdateRate(existingRate, updatedRate, strategy)) {
                            await this.exchangeRateService.updateRate(currency, updatedRate);
                            results.updated.push({
                                currency,
                                oldRate: existingRate?.value,
                                newRate: updatedRate.value,
                                change: this.calculateChange(existingRate, updatedRate)
                            });
                            
                            // Alert on large changes
                            if (strategy.alertOnLargeChanges && 
                                this.isLargeChange(existingRate, updatedRate, strategy.changeThreshold)) {
                                await this.sendRateChangeAlert(currency, existingRate, updatedRate);
                            }
                        } else {
                            results.unchanged.push(currency);
                        }
                    }
                } catch (error) {
                    results.failed.push({ currency, error: error.message });
                }
            }
            
            const duration = Date.now() - startTime;
            console.log(`Rate update completed in ${duration}ms:`, results);
            
        } catch (error) {
            console.error('Rate update failed:', error);
            throw error;
        }
        
        return results;
    }
    
    shouldUpdateRate(existingRate, newRate, strategy) {
        if (!existingRate) return true;
        
        const timeDiff = newRate.timestamp - existingRate.timestamp;
        const minUpdateInterval = strategy.updateFrequency * 0.5; // 50% of update frequency
        
        if (timeDiff < minUpdateInterval) return false;
        
        const rateDiff = Math.abs(newRate.value - existingRate.value) / existingRate.value;
        const minChangeThreshold = 0.001; // 0.1%
        
        return rateDiff >= minChangeThreshold;
    }
    
    calculateChange(oldRate, newRate) {
        if (!oldRate) return null;
        
        const change = ((newRate.value - oldRate.value) / oldRate.value) * 100;
        return {
            absolute: newRate.value - oldRate.value,
            percentage: change,
            direction: change > 0 ? 'up' : change < 0 ? 'down' : 'unchanged'
        };
    }
    
    isLargeChange(oldRate, newRate, threshold) {
        if (!oldRate) return false;
        
        const change = Math.abs(((newRate.value - oldRate.value) / oldRate.value) * 100);
        return change >= threshold;
    }
    
    async sendRateChangeAlert(currency, oldRate, newRate) {
        const change = this.calculateChange(oldRate, newRate);
        
        const alert = {
            type: 'LARGE_RATE_CHANGE',
            currency,
            oldRate: oldRate.value,
            newRate: newRate.value,
            change: change.percentage,
            timestamp: new Date().toISOString(),
            severity: Math.abs(change.percentage) > 5 ? 'HIGH' : 'MEDIUM'
        };
        
        // Send to monitoring system
        await this.sendAlert(alert);
    }
}
```

### Currency Conversion Service

```javascript
import { CurrencyConverter, ConversionRules, FeeCalculator } from '@exalt/currency-exchange-service';

class CurrencyConversionService {
    constructor(exchangeRateService, cacheManager) {
        this.exchangeRateService = exchangeRateService;
        this.cacheManager = cacheManager;
        this.setupConversionRules();
        this.setupFeeCalculator();
    }
    
    setupConversionRules() {
        this.conversionRules = new ConversionRules({
            precision: 6,
            roundingMode: 'HALF_UP',
            minimumAmount: {
                'USD': 0.01,
                'EUR': 0.01,
                'JPY': 1,
                'KRW': 1
            },
            maximumAmount: {
                'USD': 1000000,
                'EUR': 1000000,
                'JPY': 100000000,
                'default': 1000000
            },
            restrictedPairs: [
                // Pairs that require special handling
                { from: 'KPW', to: '*', reason: 'sanctions' },
                { from: '*', to: 'KPW', reason: 'sanctions' }
            ]
        });
    }
    
    setupFeeCalculator() {
        this.feeCalculator = new FeeCalculator({
            baseFeePercentage: 0.5, // 0.5% base fee
            minimumFee: {
                'USD': 0.50,
                'EUR': 0.45,
                'GBP': 0.40,
                'default': 0.50
            },
            maximumFee: {
                'USD': 50.00,
                'EUR': 45.00,
                'GBP': 40.00,
                'default': 50.00
            },
            premiumCurrencies: {
                'BTC': 2.0, // 2% fee for Bitcoin
                'ETH': 1.5, // 1.5% fee for Ethereum
                'XRP': 1.0  // 1% fee for Ripple
            },
            volumeDiscounts: [
                { threshold: 10000, discount: 0.1 }, // 10% discount for $10k+
                { threshold: 100000, discount: 0.2 }, // 20% discount for $100k+
                { threshold: 1000000, discount: 0.3 } // 30% discount for $1M+
            ]
        });
    }
    
    async convertCurrency(amount, fromCurrency, toCurrency, options = {}) {
        try {
            // Validate input
            this.validateConversionRequest(amount, fromCurrency, toCurrency);
            
            // Check for same currency
            if (fromCurrency === toCurrency) {
                return {
                    originalAmount: amount,
                    convertedAmount: amount,
                    exchangeRate: 1,
                    fromCurrency,
                    toCurrency,
                    fees: 0,
                    timestamp: new Date().toISOString()
                };
            }
            
            // Get exchange rate
            const exchangeRate = await this.getExchangeRateForConversion(fromCurrency, toCurrency);
            
            // Calculate conversion
            const baseConvertedAmount = amount * exchangeRate.value;
            
            // Calculate fees
            const fees = this.calculateConversionFees(amount, fromCurrency, toCurrency, options);
            
            // Apply fees
            const finalConvertedAmount = baseConvertedAmount - fees.totalFee;
            
            // Round to appropriate precision
            const roundedAmount = this.roundToPrecision(finalConvertedAmount, toCurrency);
            
            const result = {
                originalAmount: amount,
                convertedAmount: roundedAmount,
                exchangeRate: exchangeRate.value,
                fromCurrency,
                toCurrency,
                fees: {
                    amount: fees.totalFee,
                    percentage: fees.feePercentage,
                    breakdown: fees.breakdown
                },
                metadata: {
                    rateSource: exchangeRate.source,
                    rateTimestamp: exchangeRate.timestamp,
                    conversionTimestamp: new Date().toISOString(),
                    spread: exchangeRate.spread,
                    confidence: exchangeRate.confidence
                }
            };
            
            // Cache the conversion result
            if (options.enableCaching !== false) {
                await this.cacheConversionResult(result);
            }
            
            return result;
            
        } catch (error) {
            console.error(`Currency conversion failed for ${amount} ${fromCurrency} to ${toCurrency}:`, error);
            throw error;
        }
    }
    
    async batchConvertCurrency(conversions) {
        const results = await Promise.allSettled(
            conversions.map(({ amount, fromCurrency, toCurrency, options }) =>
                this.convertCurrency(amount, fromCurrency, toCurrency, options)
            )
        );
        
        return {
            successful: results
                .filter(result => result.status === 'fulfilled')
                .map(result => result.value),
            failed: results
                .filter(result => result.status === 'rejected')
                .map((result, index) => ({
                    request: conversions[index],
                    error: result.reason.message
                }))
        };
    }
    
    async getExchangeRateForConversion(fromCurrency, toCurrency) {
        // Try direct rate first
        let rate = await this.exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
        
        if (!rate) {
            // Try inverse rate
            const inverseRate = await this.exchangeRateService.getExchangeRate(toCurrency, fromCurrency);
            if (inverseRate) {
                rate = {
                    value: 1 / inverseRate.value,
                    source: inverseRate.source,
                    timestamp: inverseRate.timestamp,
                    spread: inverseRate.spread,
                    confidence: inverseRate.confidence
                };
            }
        }
        
        if (!rate) {
            // Try cross-rate through USD
            const fromUsdRate = await this.exchangeRateService.getExchangeRate('USD', fromCurrency);
            const toUsdRate = await this.exchangeRateService.getExchangeRate('USD', toCurrency);
            
            if (fromUsdRate && toUsdRate) {
                rate = {
                    value: toUsdRate.value / fromUsdRate.value,
                    source: 'cross-rate',
                    timestamp: Math.min(fromUsdRate.timestamp, toUsdRate.timestamp),
                    spread: fromUsdRate.spread + toUsdRate.spread,
                    confidence: Math.min(fromUsdRate.confidence, toUsdRate.confidence)
                };
            }
        }
        
        if (!rate) {
            throw new Error(`Exchange rate not available for ${fromCurrency}/${toCurrency}`);
        }
        
        return rate;
    }
    
    calculateConversionFees(amount, fromCurrency, toCurrency, options) {
        let feePercentage = this.feeCalculator.baseFeePercentage;
        
        // Apply premium currency fees
        if (this.feeCalculator.premiumCurrencies[fromCurrency]) {
            feePercentage = Math.max(feePercentage, this.feeCalculator.premiumCurrencies[fromCurrency]);
        }
        if (this.feeCalculator.premiumCurrencies[toCurrency]) {
            feePercentage = Math.max(feePercentage, this.feeCalculator.premiumCurrencies[toCurrency]);
        }
        
        // Apply volume discounts
        const volumeDiscount = this.feeCalculator.getVolumeDiscount(amount);
        if (volumeDiscount > 0) {
            feePercentage = feePercentage * (1 - volumeDiscount);
        }
        
        // Calculate base fee
        let totalFee = amount * (feePercentage / 100);
        
        // Apply minimum fee
        const minimumFee = this.feeCalculator.minimumFee[fromCurrency] || 
                          this.feeCalculator.minimumFee.default;
        totalFee = Math.max(totalFee, minimumFee);
        
        // Apply maximum fee
        const maximumFee = this.feeCalculator.maximumFee[fromCurrency] || 
                          this.feeCalculator.maximumFee.default;
        totalFee = Math.min(totalFee, maximumFee);
        
        return {
            totalFee,
            feePercentage,
            breakdown: {
                baseFee: amount * (this.feeCalculator.baseFeePercentage / 100),
                premiumFee: totalFee - (amount * (this.feeCalculator.baseFeePercentage / 100)),
                volumeDiscount: volumeDiscount * 100,
                minimumFeeApplied: totalFee === minimumFee,
                maximumFeeApplied: totalFee === maximumFee
            }
        };
    }
    
    validateConversionRequest(amount, fromCurrency, toCurrency) {
        if (!amount || amount <= 0) {
            throw new Error('Amount must be a positive number');
        }
        
        if (!fromCurrency || !toCurrency) {
            throw new Error('Both source and target currencies are required');
        }
        
        // Check minimum amount
        const minAmount = this.conversionRules.minimumAmount[fromCurrency] || 
                         this.conversionRules.minimumAmount.default || 0.01;
        if (amount < minAmount) {
            throw new Error(`Minimum amount for ${fromCurrency} is ${minAmount}`);
        }
        
        // Check maximum amount
        const maxAmount = this.conversionRules.maximumAmount[fromCurrency] || 
                         this.conversionRules.maximumAmount.default;
        if (amount > maxAmount) {
            throw new Error(`Maximum amount for ${fromCurrency} is ${maxAmount}`);
        }
        
        // Check restricted pairs
        for (const restriction of this.conversionRules.restrictedPairs) {
            if ((restriction.from === fromCurrency || restriction.from === '*') &&
                (restriction.to === toCurrency || restriction.to === '*')) {
                throw new Error(`Conversion from ${fromCurrency} to ${toCurrency} is restricted: ${restriction.reason}`);
            }
        }
    }
    
    roundToPrecision(amount, currency) {
        const precision = this.getPrecisionForCurrency(currency);
        return Math.round(amount * Math.pow(10, precision)) / Math.pow(10, precision);
    }
    
    getPrecisionForCurrency(currency) {
        const precisionMap = {
            'JPY': 0, // No decimal places for Japanese Yen
            'KRW': 0, // No decimal places for Korean Won
            'BTC': 8, // 8 decimal places for Bitcoin
            'ETH': 6, // 6 decimal places for Ethereum
            'default': 2 // 2 decimal places for most currencies
        };
        
        return precisionMap[currency] || precisionMap.default;
    }
    
    async cacheConversionResult(result) {
        const cacheKey = `conversion:${result.fromCurrency}:${result.toCurrency}:${result.originalAmount}`;
        await this.cacheManager.set(cacheKey, result, 60); // Cache for 1 minute
    }
}
```

### Historical Rate Service

```javascript
import { HistoricalRateService, DataAggregator, TrendAnalyzer } from '@exalt/currency-exchange-service';

class HistoricalRateManager {
    constructor(storageBackend, exchangeRateService) {
        this.storageBackend = storageBackend;
        this.exchangeRateService = exchangeRateService;
        this.dataAggregator = new DataAggregator();
        this.trendAnalyzer = new TrendAnalyzer();
        this.setupDataRetention();
    }
    
    setupDataRetention() {
        this.retentionPolicies = {
            raw: {
                duration: 30, // days
                interval: 'minute'
            },
            hourly: {
                duration: 90, // days
                interval: 'hour'
            },
            daily: {
                duration: 365 * 5, // 5 years
                interval: 'day'
            },
            weekly: {
                duration: 365 * 10, // 10 years
                interval: 'week'
            },
            monthly: {
                duration: 365 * 20, // 20 years
                interval: 'month'
            }
        };
    }
    
    async storeRateData(currency, rate, timestamp = new Date()) {
        try {
            const rateData = {
                currency,
                baseCurrency: 'USD',
                rate: rate.value,
                source: rate.source,
                timestamp,
                metadata: {
                    spread: rate.spread,
                    confidence: rate.confidence,
                    volume: rate.volume
                }
            };
            
            // Store raw data
            await this.storageBackend.insertRateData(rateData);
            
            // Trigger aggregation if needed
            await this.checkAndRunAggregation(currency, timestamp);
            
        } catch (error) {
            console.error(`Failed to store rate data for ${currency}:`, error);
            throw error;
        }
    }
    
    async getHistoricalRates(currency, startDate, endDate, interval = 'daily') {
        try {
            const tableName = this.getTableNameForInterval(interval);
            
            const rates = await this.storageBackend.query({
                table: tableName,
                where: {
                    currency,
                    timestamp: {
                        $gte: startDate,
                        $lte: endDate
                    }
                },
                orderBy: 'timestamp ASC'
            });
            
            return rates.map(rate => ({
                currency: rate.currency,
                rate: rate.rate,
                timestamp: rate.timestamp,
                interval,
                metadata: rate.metadata
            }));
            
        } catch (error) {
            console.error(`Failed to get historical rates for ${currency}:`, error);
            throw error;
        }
    }
    
    async getRateStats(currency, period = '30d') {
        try {
            const endDate = new Date();
            const startDate = this.calculateStartDate(endDate, period);
            
            const rates = await this.getHistoricalRates(currency, startDate, endDate, 'daily');
            
            if (rates.length === 0) {
                return null;
            }
            
            const values = rates.map(r => r.rate);
            
            const stats = {
                currency,
                period,
                count: values.length,
                latest: values[values.length - 1],
                highest: Math.max(...values),
                lowest: Math.min(...values),
                average: values.reduce((sum, val) => sum + val, 0) / values.length,
                median: this.calculateMedian(values),
                standardDeviation: this.calculateStandardDeviation(values),
                volatility: this.calculateVolatility(values),
                trend: this.trendAnalyzer.analyzeTrend(rates),
                change: {
                    absolute: values[values.length - 1] - values[0],
                    percentage: ((values[values.length - 1] - values[0]) / values[0]) * 100
                }
            };
            
            return stats;
            
        } catch (error) {
            console.error(`Failed to get rate stats for ${currency}:`, error);
            throw error;
        }
    }
    
    async getTrendAnalysis(currency, period = '90d') {
        try {
            const endDate = new Date();
            const startDate = this.calculateStartDate(endDate, period);
            
            const rates = await this.getHistoricalRates(currency, startDate, endDate, 'daily');
            
            const analysis = this.trendAnalyzer.performFullAnalysis(rates);
            
            return {
                currency,
                period,
                trend: analysis.trend,
                strength: analysis.strength,
                direction: analysis.direction,
                supportLevels: analysis.supportLevels,
                resistanceLevels: analysis.resistanceLevels,
                movingAverages: {
                    sma20: analysis.sma20,
                    sma50: analysis.sma50,
                    sma200: analysis.sma200
                },
                technicalIndicators: {
                    rsi: analysis.rsi,
                    macd: analysis.macd,
                    bollinger: analysis.bollingerBands
                },
                forecast: analysis.forecast
            };
            
        } catch (error) {
            console.error(`Failed to get trend analysis for ${currency}:`, error);
            throw error;
        }
    }
    
    async runDataAggregation(interval = 'hourly') {
        try {
            const aggregationConfig = this.retentionPolicies[interval];
            if (!aggregationConfig) {
                throw new Error(`Invalid aggregation interval: ${interval}`);
            }
            
            const cutoffDate = new Date();
            cutoffDate.setDate(cutoffDate.getDate() - 1); // Aggregate data older than 1 day
            
            const currencies = await this.storageBackend.getDistinctCurrencies();
            
            for (const currency of currencies) {
                await this.aggregateCurrencyData(currency, interval, cutoffDate);
            }
            
            console.log(`Data aggregation completed for interval: ${interval}`);
            
        } catch (error) {
            console.error(`Data aggregation failed for interval ${interval}:`, error);
            throw error;
        }
    }
    
    async aggregateCurrencyData(currency, interval, cutoffDate) {
        const startDate = new Date(cutoffDate);
        startDate.setDate(startDate.getDate() - 7); // Process last 7 days
        
        const rawData = await this.storageBackend.getRawData(currency, startDate, cutoffDate);
        
        const aggregatedData = this.dataAggregator.aggregate(rawData, interval);
        
        const tableName = this.getTableNameForInterval(interval);
        
        for (const aggregatedRate of aggregatedData) {
            await this.storageBackend.upsert(tableName, {
                currency: aggregatedRate.currency,
                timestamp: aggregatedRate.timestamp,
                rate: aggregatedRate.rate,
                high: aggregatedRate.high,
                low: aggregatedRate.low,
                open: aggregatedRate.open,
                close: aggregatedRate.close,
                volume: aggregatedRate.volume,
                count: aggregatedRate.count
            });
        }
    }
    
    async cleanupOldData() {
        try {
            const now = new Date();
            
            for (const [interval, policy] of Object.entries(this.retentionPolicies)) {
                const cutoffDate = new Date(now);
                cutoffDate.setDate(cutoffDate.getDate() - policy.duration);
                
                const tableName = this.getTableNameForInterval(interval);
                
                const deletedCount = await this.storageBackend.deleteOldData(tableName, cutoffDate);
                
                console.log(`Cleaned up ${deletedCount} old records from ${tableName}`);
            }
            
        } catch (error) {
            console.error('Failed to cleanup old data:', error);
            throw error;
        }
    }
    
    calculateStartDate(endDate, period) {
        const startDate = new Date(endDate);
        
        if (period.endsWith('d')) {
            const days = parseInt(period.slice(0, -1));
            startDate.setDate(startDate.getDate() - days);
        } else if (period.endsWith('w')) {
            const weeks = parseInt(period.slice(0, -1));
            startDate.setDate(startDate.getDate() - (weeks * 7));
        } else if (period.endsWith('m')) {
            const months = parseInt(period.slice(0, -1));
            startDate.setMonth(startDate.getMonth() - months);
        } else if (period.endsWith('y')) {
            const years = parseInt(period.slice(0, -1));
            startDate.setFullYear(startDate.getFullYear() - years);
        }
        
        return startDate;
    }
    
    getTableNameForInterval(interval) {
        const tableMap = {
            'raw': 'rates_raw',
            'minute': 'rates_raw',
            'hourly': 'rates_hourly',
            'daily': 'rates_daily',
            'weekly': 'rates_weekly',
            'monthly': 'rates_monthly'
        };
        
        return tableMap[interval] || 'rates_daily';
    }
    
    calculateMedian(values) {
        const sorted = [...values].sort((a, b) => a - b);
        const mid = Math.floor(sorted.length / 2);
        return sorted.length % 2 === 0 
            ? (sorted[mid - 1] + sorted[mid]) / 2 
            : sorted[mid];
    }
    
    calculateStandardDeviation(values) {
        const mean = values.reduce((sum, val) => sum + val, 0) / values.length;
        const squaredDiffs = values.map(val => Math.pow(val - mean, 2));
        const avgSquaredDiff = squaredDiffs.reduce((sum, val) => sum + val, 0) / values.length;
        return Math.sqrt(avgSquaredDiff);
    }
    
    calculateVolatility(values) {
        if (values.length < 2) return 0;
        
        const returns = [];
        for (let i = 1; i < values.length; i++) {
            returns.push(Math.log(values[i] / values[i - 1]));
        }
        
        return this.calculateStandardDeviation(returns) * Math.sqrt(252); // Annualized volatility
    }
}
```

## API Reference

### Core Exchange Rate API

#### ExchangeRateService
- `ExchangeRateService(config)`: Initialize exchange rate service with configuration
- `getExchangeRate(baseCurrency, targetCurrency)`: Get current exchange rate between currencies
- `getMultipleRates(baseCurrency, targetCurrencies)`: Get multiple exchange rates from base currency
- `getAllRates()`: Get all available exchange rates
- `updateRate(currency, rate)`: Update exchange rate for specific currency
- `getLastUpdate(currency)`: Get timestamp of last rate update
- `isRateStale(currency, threshold)`: Check if rate is older than threshold
- `getSupportedCurrencies()`: Get list of supported currencies
- `startScheduler()`: Start automatic rate update scheduler
- `stopScheduler()`: Stop automatic rate update scheduler

#### CurrencyConverter
- `CurrencyConverter(exchangeRateService, config)`: Initialize currency converter
- `convert(amount, fromCurrency, toCurrency, options)`: Convert amount between currencies
- `convertWithFees(amount, fromCurrency, toCurrency, feeConfig)`: Convert with fee calculation
- `batchConvert(conversions)`: Convert multiple currency amounts in batch
- `getConversionRate(fromCurrency, toCurrency)`: Get conversion rate with fees
- `validateConversion(amount, fromCurrency, toCurrency)`: Validate conversion request
- `roundToPrecision(amount, currency)`: Round amount to currency precision
- `calculateFees(amount, fromCurrency, toCurrency)`: Calculate conversion fees

#### ProviderManager
- `ProviderManager(providers, config)`: Initialize provider manager
- `fetchRate(currency, providers)`: Fetch rate from specified providers
- `fetchRateWithFallback(currency)`: Fetch rate with provider fallback
- `validateRate(rate, rules)`: Validate rate against rules
- `aggregateRates(rates)`: Aggregate rates from multiple providers
- `getProviderStatus(providerName)`: Get status of specific provider
- `enableProvider(providerName)`: Enable data provider
- `disableProvider(providerName)`: Disable data provider
- `getProviderPriority()`: Get provider priority order

### Cache Management API

#### RedisCacheManager
- `RedisCacheManager(config)`: Initialize Redis cache manager
- `connect()`: Connect to Redis server
- `disconnect()`: Disconnect from Redis server
- `get(key)`: Get value from cache
- `set(key, value, ttl)`: Set value in cache with TTL
- `delete(key)`: Delete key from cache
- `exists(key)`: Check if key exists in cache
- `clear()`: Clear all cache entries
- `getStats()`: Get cache statistics
- `setTtl(key, ttl)`: Update TTL for existing key

#### CacheInvalidationService
- `CacheInvalidationService(cacheManager)`: Initialize cache invalidation service
- `invalidateRate(currency)`: Invalidate cached rate for currency
- `invalidateAllRates()`: Invalidate all cached rates
- `invalidatePattern(pattern)`: Invalidate keys matching pattern
- `scheduleInvalidation(key, delay)`: Schedule future invalidation
- `getInvalidationEvents()`: Get invalidation event history

### Historical Data API

#### HistoricalRateService
- `HistoricalRateService(config)`: Initialize historical rate service
- `storeRate(currency, rate, timestamp)`: Store historical rate data
- `getRates(currency, startDate, endDate, interval)`: Get historical rates
- `getStats(currency, period)`: Get statistical analysis of rates
- `getTrend(currency, period)`: Get trend analysis
- `aggregate(interval)`: Run data aggregation
- `cleanup()`: Clean up old data according to retention policy

#### TrendAnalyzer
- `TrendAnalyzer()`: Initialize trend analyzer
- `analyzeTrend(rates)`: Analyze trend in rate data
- `calculateMovingAverage(rates, period)`: Calculate moving average
- `calculateRSI(rates, period)`: Calculate Relative Strength Index
- `calculateMACD(rates)`: Calculate MACD indicator
- `calculateBollingerBands(rates, period)`: Calculate Bollinger Bands
- `forecast(rates, periods)`: Generate rate forecast

### API Controllers

#### ExchangeRateController
- `GET /api/v1/rates`: Get all current exchange rates
- `GET /api/v1/rates/:base`: Get rates for base currency
- `GET /api/v1/rates/:base/:target`: Get specific exchange rate
- `POST /api/v1/rates/multiple`: Get multiple exchange rates
- `GET /api/v1/rates/supported`: Get supported currencies

#### ConversionController
- `POST /api/v1/convert`: Convert currency amount
- `POST /api/v1/convert/batch`: Batch currency conversion
- `GET /api/v1/convert/rate/:from/:to`: Get conversion rate
- `POST /api/v1/convert/fees`: Calculate conversion fees

#### HistoricalController
- `GET /api/v1/historical/:currency`: Get historical rates
- `GET /api/v1/historical/:currency/stats`: Get rate statistics
- `GET /api/v1/historical/:currency/trend`: Get trend analysis
- `GET /api/v1/historical/:currency/forecast`: Get rate forecast

#### HealthController
- `GET /health`: Service health status
- `GET /health/live`: Liveness probe
- `GET /health/ready`: Readiness probe
- `GET /health/providers`: Provider health status

## Best Practices

1. **Rate Management**: Implement proper rate validation and fallback mechanisms
2. **Caching Strategy**: Use appropriate TTL values based on currency volatility
3. **Error Handling**: Handle provider failures gracefully with fallback options
4. **Performance**: Optimize for high-frequency conversion requests
5. **Security**: Validate all input and implement rate limiting
6. **Monitoring**: Track rate changes, conversion volumes, and service performance
7. **Data Retention**: Implement proper data retention and archival policies

## Related Documentation

- [API Specification](../api-docs/openapi.yaml)
- [Architecture Documentation](./architecture/README.md)
- [Setup Guide](./setup/README.md)
- [Operations Guide](./operations/README.md)