require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const winston = require('winston');
const NodeCache = require('node-cache');
const Joi = require('joi');
const { v4: uuidv4 } = require('uuid');

const app = express();

// Middleware
app.use(helmet());
app.use(cors());
app.use(bodyParser.json());
app.use(morgan('combined'));

// Logger
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'feature-flags.log' })
  ]
});

// Cache for flags - 5 minutes TTL
const cache = new NodeCache({ stdTTL: 300 });

// Validation schemas
const flagSchema = Joi.object({
  enabled: Joi.boolean().required(),
  percentage: Joi.number().min(0).max(100).required(),
  description: Joi.string().optional(),
  conditions: Joi.object().optional()
});

const checkFlagSchema = Joi.object({
  userId: Joi.string().optional(),
  userContext: Joi.object().optional()
});

// Default feature flags
const defaultFlags = {
  'social-media-integration': { 
    enabled: true, 
    percentage: 100,
    description: 'Enable social media platform integration'
  },
  'advanced-analytics': { 
    enabled: false, 
    percentage: 0,
    description: 'Enable advanced analytics features'
  },
  'multi-currency': { 
    enabled: true, 
    percentage: 100,
    description: 'Enable multi-currency support'
  },
  'real-time-notifications': { 
    enabled: true, 
    percentage: 50,
    description: 'Enable real-time push notifications'
  },
  'ai-recommendations': { 
    enabled: false, 
    percentage: 10,
    description: 'Enable AI-powered product recommendations'
  },
  'dark-mode': {
    enabled: true,
    percentage: 75,
    description: 'Enable dark mode UI theme'
  },
  'beta-features': {
    enabled: true,
    percentage: 5,
    description: 'Enable beta features for early adopters'
  }
};

// Initialize cache with default flags
cache.set('flags', defaultFlags);

// Helper function to determine if flag is enabled for user
function isEnabled(flag, userId, userContext = {}) {
  if (!flag.enabled) return false;
  
  // Check conditions if they exist
  if (flag.conditions) {
    // Future: implement more complex conditions
  }
  
  // Simple percentage-based rollout using user ID hash
  if (userId) {
    const userHash = userId.split('').reduce((a, b) => {
      a = ((a << 5) - a) + b.charCodeAt(0);
      return a & a;
    }, 0);
    
    return (Math.abs(userHash) % 100) < flag.percentage;
  }
  
  // Random rollout if no user ID
  return Math.random() * 100 < flag.percentage;
}

// Get all feature flags
app.get('/flags', (req, res) => {
  try {
    const flags = cache.get('flags') || defaultFlags;
    logger.info('Retrieved all feature flags');
    res.json({
      flags,
      timestamp: new Date().toISOString(),
      count: Object.keys(flags).length
    });
  } catch (error) {
    logger.error('Error retrieving flags:', error);
    res.status(500).json({ error: 'Failed to retrieve flags' });
  }
});

// Get specific flag
app.get('/flags/:flagName', (req, res) => {
  try {
    const flags = cache.get('flags') || defaultFlags;
    const flag = flags[req.params.flagName];
    
    if (!flag) {
      return res.status(404).json({ 
        error: 'Flag not found',
        availableFlags: Object.keys(flags)
      });
    }
    
    logger.info(`Retrieved flag: ${req.params.flagName}`);
    res.json({
      flagName: req.params.flagName,
      ...flag,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Error retrieving flag:', error);
    res.status(500).json({ error: 'Failed to retrieve flag' });
  }
});

// Check if flag is enabled for user
app.post('/flags/:flagName/check', (req, res) => {
  try {
    const { error, value } = checkFlagSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }
    
    const { userId, userContext } = value;
    const flags = cache.get('flags') || defaultFlags;
    const flag = flags[req.params.flagName];
    
    if (!flag) {
      return res.status(404).json({ error: 'Flag not found' });
    }
    
    const enabled = isEnabled(flag, userId, userContext);
    const checkId = uuidv4();
    
    const result = {
      checkId,
      flagName: req.params.flagName,
      enabled,
      userId: userId || null,
      timestamp: new Date().toISOString(),
      flag: {
        enabled: flag.enabled,
        percentage: flag.percentage
      }
    };
    
    logger.info(`Flag check: ${req.params.flagName} for user ${userId}: ${enabled}`, {
      checkId,
      flagName: req.params.flagName,
      userId,
      enabled
    });
    
    res.json(result);
  } catch (error) {
    logger.error('Error checking flag:', error);
    res.status(500).json({ error: 'Failed to check flag' });
  }
});

// Bulk flag check
app.post('/flags/check', (req, res) => {
  try {
    const { error, value } = checkFlagSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }
    
    const { userId, userContext } = value;
    const flags = cache.get('flags') || defaultFlags;
    const results = {};
    
    Object.keys(flags).forEach(flagName => {
      results[flagName] = isEnabled(flags[flagName], userId, userContext);
    });
    
    logger.info(`Bulk flag check for user ${userId}`, { userId, flagCount: Object.keys(results).length });
    
    res.json({
      userId: userId || null,
      flags: results,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Error in bulk flag check:', error);
    res.status(500).json({ error: 'Failed to check flags' });
  }
});

// Update flag (admin only - TODO: add proper authentication)
app.put('/flags/:flagName', (req, res) => {
  try {
    const { error, value } = flagSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }
    
    const flags = cache.get('flags') || defaultFlags;
    const flagName = req.params.flagName;
    
    if (!flags[flagName]) {
      return res.status(404).json({ error: 'Flag not found' });
    }
    
    flags[flagName] = {
      ...flags[flagName],
      ...value,
      updatedAt: new Date().toISOString()
    };
    
    cache.set('flags', flags);
    
    logger.info(`Flag updated: ${flagName}`, flags[flagName]);
    
    res.json({
      flagName,
      ...flags[flagName],
      message: 'Flag updated successfully'
    });
  } catch (error) {
    logger.error('Error updating flag:', error);
    res.status(500).json({ error: 'Failed to update flag' });
  }
});

// Create new flag
app.post('/flags/:flagName', (req, res) => {
  try {
    const { error, value } = flagSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }
    
    const flags = cache.get('flags') || defaultFlags;
    const flagName = req.params.flagName;
    
    if (flags[flagName]) {
      return res.status(409).json({ error: 'Flag already exists' });
    }
    
    flags[flagName] = {
      ...value,
      createdAt: new Date().toISOString()
    };
    
    cache.set('flags', flags);
    
    logger.info(`Flag created: ${flagName}`, flags[flagName]);
    
    res.status(201).json({
      flagName,
      ...flags[flagName],
      message: 'Flag created successfully'
    });
  } catch (error) {
    logger.error('Error creating flag:', error);
    res.status(500).json({ error: 'Failed to create flag' });
  }
});

// Delete flag
app.delete('/flags/:flagName', (req, res) => {
  try {
    const flags = cache.get('flags') || defaultFlags;
    const flagName = req.params.flagName;
    
    if (!flags[flagName]) {
      return res.status(404).json({ error: 'Flag not found' });
    }
    
    delete flags[flagName];
    cache.set('flags', flags);
    
    logger.info(`Flag deleted: ${flagName}`);
    
    res.json({ message: `Flag ${flagName} deleted successfully` });
  } catch (error) {
    logger.error('Error deleting flag:', error);
    res.status(500).json({ error: 'Failed to delete flag' });
  }
});

// Health check
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    service: 'feature-flags',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// Metrics endpoint
app.get('/metrics', (req, res) => {
  try {
    const flags = cache.get('flags') || defaultFlags;
    const cacheStats = cache.getStats();
    
    res.json({
      service: 'feature-flags',
      version: '1.0.0',
      uptime: process.uptime(),
      memory: process.memoryUsage(),
      flagCount: Object.keys(flags).length,
      cache: cacheStats,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    logger.error('Error retrieving metrics:', error);
    res.status(500).json({ error: 'Failed to retrieve metrics' });
  }
});

// Error handling middleware
app.use((error, req, res, next) => {
  logger.error('Unhandled error:', error);
  res.status(500).json({ error: 'Internal server error' });
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  logger.info(`Feature Flag Service running on port ${PORT}`);
  console.log(`Feature Flag Service running on port ${PORT}`);
});

module.exports = app; // For testing