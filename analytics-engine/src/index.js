const express = require('express');
const bodyParser = require('body-parser');
const { Kafka } = require('kafkajs');
const Redis = require('ioredis');
const mongoose = require('mongoose');
const cors = require('cors');
const morgan = require('morgan');
const helmet = require('helmet');
const winston = require('winston');
const app = express();

// Logger configuration
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'analytics-engine.log' })
  ]
});

// Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('combined'));
app.use(bodyParser.json());

// Connect to Redis
const redis = new Redis({
  host: process.env.REDIS_HOST || 'localhost',
  port: process.env.REDIS_PORT || 6379,
});

// Connect to MongoDB
mongoose.connect(process.env.MONGO_URI || 'mongodb://localhost:27017/analytics-engine', {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  logger.info('MongoDB connected successfully');
}).catch(err => {
  logger.error('MongoDB connection error:', err);
});

// Define MongoDB schemas
const metricsSchema = new mongoose.Schema({
  type: String,
  timestamp: Date,
  data: mongoose.Schema.Types.Mixed,
  metadata: mongoose.Schema.Types.Mixed
}, { timestamps: true });

const Metric = mongoose.model('Metric', metricsSchema);

// In-memory store for recent metrics (for quick access)
const recentMetrics = {
  sales: [], // { date, orderId, userId, amount, products }
  inventory: [], // { date, productId, quantity, location }
  performance: [], // { date, service, latency, errorRate, throughput }
  user: [], // { date, userId, action, duration, platform }
  social: [] // { date, userId, action, content, engagement }
};

// Kafka consumer setup
const kafka = new Kafka({
  clientId: 'analytics-engine',
  brokers: [process.env.KAFKA_BROKER || 'localhost:9092']
});
const consumer = kafka.consumer({ groupId: 'analytics-group' });

// Process and store a new metric event
async function processMetricEvent(event) {
  try {
    // Create a new metric document
    const metric = new Metric({
      type: event.type,
      timestamp: event.timestamp || new Date(),
      data: event.data || {},
      metadata: event.metadata || {}
    });
    
    // Store in MongoDB
    await metric.save();
    
    // Also store in in-memory cache based on event type
    switch (event.type) {
      case 'order.placed':
        recentMetrics.sales.push({
          date: event.timestamp || new Date(),
          orderId: event.data.orderId,
          userId: event.data.userId,
          amount: event.data.amount || 0,
          products: event.data.products || []
        });
        break;
        
      case 'inventory.updated':
        recentMetrics.inventory.push({
          date: event.timestamp || new Date(),
          productId: event.data.productId,
          quantity: event.data.quantity,
          location: event.data.location
        });
        break;
        
      case 'performance.metric':
        recentMetrics.performance.push({
          date: event.timestamp || new Date(),
          service: event.data.service,
          latency: event.data.latency,
          errorRate: event.data.errorRate,
          throughput: event.data.throughput
        });
        break;
        
      case 'user.activity':
        recentMetrics.user.push({
          date: event.timestamp || new Date(),
          userId: event.data.userId,
          action: event.data.action,
          duration: event.data.duration,
          platform: event.data.platform
        });
        break;
        
      case 'social.engagement':
        recentMetrics.social.push({
          date: event.timestamp || new Date(),
          userId: event.data.userId,
          action: event.data.action,
          content: event.data.content,
          engagement: event.data.engagement
        });
        break;
        
      default:
        logger.warn(`Unknown event type: ${event.type}`);
    }
    
    // Cache aggregated metrics in Redis for faster access
    await cacheAggregatedMetrics(event.type);
    
    logger.info(`Processed ${event.type} event`, { id: metric._id });
  } catch (error) {
    logger.error('Error processing metric event:', error);
  }
}

// Cache aggregated metrics in Redis
async function cacheAggregatedMetrics(eventType) {
  try {
    switch (eventType) {
      case 'order.placed':
        // Aggregate daily sales
        const dailySales = aggregateDailySales();
        await redis.set('metrics:sales:daily', JSON.stringify(dailySales), 'EX', 3600); // 1 hour expiry
        // Aggregate sales by product category
        const salesByCategory = aggregateSalesByCategory();
        await redis.set('metrics:sales:by_category', JSON.stringify(salesByCategory), 'EX', 3600);
        break;
        
      case 'inventory.updated':
        // Aggregate inventory by product
        const inventoryByProduct = aggregateInventoryByProduct();
        await redis.set('metrics:inventory:by_product', JSON.stringify(inventoryByProduct), 'EX', 3600);
        // Aggregate inventory by location
        const inventoryByLocation = aggregateInventoryByLocation();
        await redis.set('metrics:inventory:by_location', JSON.stringify(inventoryByLocation), 'EX', 3600);
        break;
        
      case 'performance.metric':
        // Aggregate average metrics by service
        const performanceByService = aggregatePerformanceByService();
        await redis.set('metrics:performance:by_service', JSON.stringify(performanceByService), 'EX', 3600);
        break;
        
      case 'user.activity':
        // Aggregate user activity by action type
        const userActivityByType = aggregateUserActivityByType();
        await redis.set('metrics:user:by_activity', JSON.stringify(userActivityByType), 'EX', 3600);
        break;
        
      case 'social.engagement':
        // Aggregate social engagement metrics
        const socialEngagement = aggregateSocialEngagement();
        await redis.set('metrics:social:engagement', JSON.stringify(socialEngagement), 'EX', 3600);
        break;
    }
  } catch (error) {
    logger.error('Error caching aggregated metrics:', error);
  }
}

// Aggregation functions
function aggregateDailySales() {
  const daily = {};
  for (const sale of recentMetrics.sales) {
    const day = new Date(sale.date).toISOString().split('T')[0];
    daily[day] = (daily[day] || 0) + sale.amount;
  }
  return daily;
}

function aggregateSalesByCategory() {
  const byCategory = {};
  for (const sale of recentMetrics.sales) {
    if (sale.products && Array.isArray(sale.products)) {
      for (const product of sale.products) {
        const category = product.category || 'uncategorized';
        if (!byCategory[category]) byCategory[category] = { count: 0, revenue: 0 };
        byCategory[category].count += product.quantity || 1;
        byCategory[category].revenue += (product.price * (product.quantity || 1)) || 0;
      }
    }
  }
  return byCategory;
}

function aggregateInventoryByProduct() {
  const byProduct = {};
  for (const inv of recentMetrics.inventory) {
    byProduct[inv.productId] = inv.quantity;
  }
  return byProduct;
}

function aggregateInventoryByLocation() {
  const byLocation = {};
  for (const inv of recentMetrics.inventory) {
    if (!inv.location) continue;
    if (!byLocation[inv.location]) byLocation[inv.location] = {};
    byLocation[inv.location][inv.productId] = inv.quantity;
  }
  return byLocation;
}

function aggregatePerformanceByService() {
  const byService = {};
  for (const perf of recentMetrics.performance) {
    if (!byService[perf.service]) byService[perf.service] = { count: 0, latency: 0, errorRate: 0, throughput: 0 };
    byService[perf.service].count++;
    byService[perf.service].latency += perf.latency || 0;
    byService[perf.service].errorRate += perf.errorRate || 0;
    byService[perf.service].throughput += perf.throughput || 0;
  }
  
  // Calculate averages
  for (const svc in byService) {
    const s = byService[svc];
    s.avgLatency = s.latency / s.count;
    s.avgErrorRate = s.errorRate / s.count;
    s.avgThroughput = s.throughput / s.count;
    delete s.latency;
    delete s.errorRate;
    delete s.throughput;
    delete s.count;
  }
  return byService;
}

function aggregateUserActivityByType() {
  const byType = {};
  for (const activity of recentMetrics.user) {
    const actionType = activity.action;
    if (!byType[actionType]) byType[actionType] = { count: 0, avgDuration: 0 };
    byType[actionType].count++;
    byType[actionType].avgDuration = 
      ((byType[actionType].avgDuration * (byType[actionType].count - 1)) + (activity.duration || 0)) / 
      byType[actionType].count;
  }
  return byType;
}

function aggregateSocialEngagement() {
  const engagement = {
    byType: {},
    totalEngagements: 0,
    averageEngagementRate: 0
  };
  
  for (const social of recentMetrics.social) {
    const actionType = social.action;
    if (!engagement.byType[actionType]) engagement.byType[actionType] = 0;
    engagement.byType[actionType]++;
    engagement.totalEngagements++;
  }
  
  // Calculate average engagement rate if there are content impressions
  const impressions = engagement.byType['impression'] || 0;
  if (impressions > 0) {
    const engagements = engagement.totalEngagements - impressions;
    engagement.averageEngagementRate = engagements / impressions;
  }
  
  return engagement;
}

// Start Kafka consumer
async function startKafkaConsumer() {
  try {
    await consumer.connect();
    await consumer.subscribe({ 
      topics: [
        'order.placed',
        'inventory.updated',
        'performance.metric',
        'user.activity',
        'social.engagement'
      ], 
      fromBeginning: false 
    });
    
    await consumer.run({
      eachMessage: async ({ topic, message }) => {
        try {
          const event = JSON.parse(message.value.toString());
          event.type = topic; // Add topic as event type
          await processMetricEvent(event);
        } catch (error) {
          logger.error('Error processing Kafka message:', error);
        }
      }
    });
    
    logger.info('Kafka consumer running and ingesting events...');
  } catch (error) {
    logger.error('Error starting Kafka consumer:', error);
  }
}

// Start the Kafka consumer in the background
startKafkaConsumer().catch(err => {
  logger.error('Failed to start Kafka consumer:', err);
});

// REST endpoints
// Get sales metrics
app.get('/metrics/sales', async (req, res) => {
  try {
    // Try to get from Redis cache first
    const cachedDailySales = await redis.get('metrics:sales:daily');
    const cachedSalesByCategory = await redis.get('metrics:sales:by_category');
    
    if (cachedDailySales && cachedSalesByCategory) {
      return res.json({
        daily: JSON.parse(cachedDailySales),
        byCategory: JSON.parse(cachedSalesByCategory)
      });
    }
    
    // If not in cache, compute and return
    const daily = aggregateDailySales();
    const byCategory = aggregateSalesByCategory();
    
    res.json({ daily, byCategory });
  } catch (error) {
    logger.error('Error getting sales metrics:', error);
    res.status(500).json({ error: 'Failed to retrieve sales metrics' });
  }
});

// Get inventory metrics
app.get('/metrics/inventory', async (req, res) => {
  try {
    // Try to get from Redis cache first
    const cachedInventoryByProduct = await redis.get('metrics:inventory:by_product');
    const cachedInventoryByLocation = await redis.get('metrics:inventory:by_location');
    
    if (cachedInventoryByProduct && cachedInventoryByLocation) {
      return res.json({
        byProduct: JSON.parse(cachedInventoryByProduct),
        byLocation: JSON.parse(cachedInventoryByLocation)
      });
    }
    
    // If not in cache, compute and return
    const byProduct = aggregateInventoryByProduct();
    const byLocation = aggregateInventoryByLocation();
    
    res.json({ byProduct, byLocation });
  } catch (error) {
    logger.error('Error getting inventory metrics:', error);
    res.status(500).json({ error: 'Failed to retrieve inventory metrics' });
  }
});

// Get performance metrics
app.get('/metrics/performance', async (req, res) => {
  try {
    // Try to get from Redis cache first
    const cachedPerformanceByService = await redis.get('metrics:performance:by_service');
    
    if (cachedPerformanceByService) {
      return res.json({
        byService: JSON.parse(cachedPerformanceByService)
      });
    }
    
    // If not in cache, compute and return
    const byService = aggregatePerformanceByService();
    
    res.json({ byService });
  } catch (error) {
    logger.error('Error getting performance metrics:', error);
    res.status(500).json({ error: 'Failed to retrieve performance metrics' });
  }
});

// Get user activity metrics
app.get('/metrics/user', async (req, res) => {
  try {
    // Try to get from Redis cache first
    const cachedUserActivityByType = await redis.get('metrics:user:by_activity');
    
    if (cachedUserActivityByType) {
      return res.json({
        byActivity: JSON.parse(cachedUserActivityByType)
      });
    }
    
    // If not in cache, compute and return
    const byActivity = aggregateUserActivityByType();
    
    res.json({ byActivity });
  } catch (error) {
    logger.error('Error getting user activity metrics:', error);
    res.status(500).json({ error: 'Failed to retrieve user activity metrics' });
  }
});

// Get social engagement metrics
app.get('/metrics/social', async (req, res) => {
  try {
    // Try to get from Redis cache first
    const cachedSocialEngagement = await redis.get('metrics:social:engagement');
    
    if (cachedSocialEngagement) {
      return res.json(JSON.parse(cachedSocialEngagement));
    }
    
    // If not in cache, compute and return
    const engagement = aggregateSocialEngagement();
    
    res.json(engagement);
  } catch (error) {
    logger.error('Error getting social engagement metrics:', error);
    res.status(500).json({ error: 'Failed to retrieve social engagement metrics' });
  }
});

// Time series data endpoints for dashboard charts
app.get('/metrics/:metricType/timeseries', async (req, res) => {
  try {
    const { metricType } = req.params;
    const { startDate, endDate, interval = 'day' } = req.query;
    
    if (!startDate || !endDate) {
      return res.status(400).json({ error: 'startDate and endDate are required query parameters' });
    }
    
    // Query MongoDB for time series data
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    // Different aggregation based on the interval
    let groupId;
    if (interval === 'hour') {
      groupId = {
        year: { $year: '$timestamp' },
        month: { $month: '$timestamp' },
        day: { $dayOfMonth: '$timestamp' },
        hour: { $hour: '$timestamp' }
      };
    } else if (interval === 'day') {
      groupId = {
        year: { $year: '$timestamp' },
        month: { $month: '$timestamp' },
        day: { $dayOfMonth: '$timestamp' }
      };
    } else if (interval === 'week') {
      groupId = {
        year: { $year: '$timestamp' },
        week: { $week: '$timestamp' }
      };
    } else if (interval === 'month') {
      groupId = {
        year: { $year: '$timestamp' },
        month: { $month: '$timestamp' }
      };
    } else {
      return res.status(400).json({ error: 'Invalid interval. Supported values: hour, day, week, month' });
    }
    
    // Construct the MongoDB aggregation pipeline
    const pipeline = [
      {
        $match: {
          type: metricType,
          timestamp: { $gte: start, $lte: end }
        }
      },
      {
        $group: {
          _id: groupId,
          count: { $sum: 1 },
          data: { $push: '$data' }
        }
      },
      {
        $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1 }
      }
    ];
    
    const timeSeriesData = await Metric.aggregate(pipeline);
    
    // Format the response
    const formattedData = timeSeriesData.map(item => {
      let timestamp;
      if (interval === 'hour') {
        timestamp = new Date(
          item._id.year, 
          item._id.month - 1, 
          item._id.day, 
          item._id.hour
        ).toISOString();
      } else if (interval === 'day') {
        timestamp = new Date(
          item._id.year, 
          item._id.month - 1, 
          item._id.day
        ).toISOString().split('T')[0];
      } else if (interval === 'week') {
        // For week, use the first day of the week
        // Note: This is a simplified calculation
        const firstDayOfYear = new Date(item._id.year, 0, 1);
        const daysOffset = (item._id.week - 1) * 7;
        timestamp = new Date(firstDayOfYear.getTime() + daysOffset * 24 * 60 * 60 * 1000)
          .toISOString().split('T')[0];
      } else if (interval === 'month') {
        timestamp = `${item._id.year}-${item._id.month.toString().padStart(2, '0')}`;
      }
      
      return {
        timestamp,
        count: item.count,
        data: item.data
      };
    });
    
    res.json({
      metricType,
      interval,
      startDate: startDate,
      endDate: endDate,
      dataPoints: formattedData
    });
  } catch (error) {
    logger.error(`Error getting ${req.params.metricType} time series data:`, error);
    res.status(500).json({ error: 'Failed to retrieve time series data' });
  }
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'UP', version: '1.0.0' });
});

// Custom metrics endpoint
app.get('/metrics/custom', (req, res) => {
  res.json({ message: 'Custom metrics endpoint (to be implemented)' });
});

// Start the server
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  logger.info(`Analytics Engine running on port ${PORT}`);
}); 