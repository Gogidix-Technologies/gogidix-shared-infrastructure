require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const axios = require('axios');
const NodeCache = require('node-cache');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const winston = require('winston');

const app = express();

// Middleware
app.use(helmet());
app.use(cors());
app.use(bodyParser.json());
app.use(morgan('combined'));

// Logger
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'currency-exchange.log' })
  ]
});

// Cache for 1 hour
const cache = new NodeCache({ stdTTL: 3600 });

// Mock rates as fallback
const mockRates = {
  USD: 1, EUR: 0.92, GBP: 0.79, JPY: 155.5, NGN: 1450, ZAR: 18.5, 
  CAD: 1.35, AUD: 1.52, CHF: 0.88, CNY: 7.23
};

// Get real exchange rates with fallback
async function getRates() {
  const cached = cache.get('rates');
  if (cached) return cached;
  
  try {
    // Use a free API (exchangerate-api.com)
    const response = await axios.get('https://api.exchangerate-api.com/v4/latest/USD', {
      timeout: 5000
    });
    const rates = response.data.rates;
    cache.set('rates', rates);
    logger.info('Updated exchange rates from API');
    return rates;
  } catch (error) {
    logger.warn('Using mock rates due to API error:', error.message);
    return mockRates;
  }
}

// Get current exchange rates
app.get('/rates', async (req, res) => {
  try {
    const rates = await getRates();
    res.json({
      rates,
      timestamp: new Date().toISOString(),
      source: cache.get('rates') ? 'live' : 'mock'
    });
  } catch (error) {
    logger.error('Failed to fetch rates:', error);
    res.status(500).json({ error: 'Failed to fetch rates' });
  }
});

// Get rates for specific currencies
app.get('/rates/:currencies', async (req, res) => {
  try {
    const currencies = req.params.currencies.split(',').map(c => c.toUpperCase());
    const rates = await getRates();
    
    const filteredRates = {};
    currencies.forEach(currency => {
      if (rates[currency]) {
        filteredRates[currency] = rates[currency];
      }
    });
    
    res.json({
      rates: filteredRates,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Failed to fetch specific rates:', error);
    res.status(500).json({ error: 'Failed to fetch rates' });
  }
});

// Convert amount between currencies
app.post('/convert', async (req, res) => {
  const { amount, from, to } = req.body;
  
  if (!amount || !from || !to) {
    return res.status(400).json({ 
      error: 'amount, from, and to are required',
      example: { amount: 100, from: 'USD', to: 'EUR' }
    });
  }
  
  try {
    const rates = await getRates();
    const fromCurrency = from.toUpperCase();
    const toCurrency = to.toUpperCase();
    
    if (!rates[fromCurrency] || !rates[toCurrency]) {
      return res.status(400).json({ 
        error: 'Unsupported currency',
        supportedCurrencies: Object.keys(rates)
      });
    }
    
    // Convert to USD, then to target
    const usd = amount / rates[fromCurrency];
    const converted = usd * rates[toCurrency];
    
    const result = {
      originalAmount: amount,
      fromCurrency,
      toCurrency, 
      convertedAmount: Math.round(converted * 100) / 100,
      exchangeRate: rates[toCurrency] / rates[fromCurrency],
      timestamp: new Date().toISOString()
    };
    
    logger.info('Currency conversion', result);
    res.json(result);
  } catch (error) {
    logger.error('Conversion failed:', error);
    res.status(500).json({ error: 'Conversion failed' });
  }
});

// Get historical rates (mock for now)
app.get('/historical/:date', (req, res) => {
  const { date } = req.params;
  res.json({
    date,
    rates: mockRates,
    note: 'Historical rates feature coming soon'
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    service: 'currency-exchange',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// Metrics endpoint
app.get('/metrics', (req, res) => {
  const cacheStats = cache.getStats();
  res.json({
    service: 'currency-exchange',
    uptime: process.uptime(),
    memory: process.memoryUsage(),
    cache: cacheStats
  });
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  logger.info(`Currency Exchange Service running on port ${PORT}`);
  console.log(`Currency Exchange Service running on port ${PORT}`);
});

module.exports = app; // For testing