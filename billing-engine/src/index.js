require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const winston = require('winston');
const { v4: uuidv4 } = require('uuid');
const Joi = require('joi');
const Decimal = require('decimal.js');
const moment = require('moment');
const NodeCache = require('node-cache');

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
    new winston.transports.File({ filename: 'billing.log' })
  ]
});

// Cache for frequent queries
const cache = new NodeCache({ stdTTL: 600 }); // 10 minutes

// In-memory stores (in production, use proper database)
const invoices = {};
const payments = {};
const subscriptions = {};
const billingProfiles = {};

// Validation schemas
const invoiceSchema = Joi.object({
  customerId: Joi.string().required(),
  customerEmail: Joi.string().email().required(),
  items: Joi.array().items(Joi.object({
    description: Joi.string().required(),
    quantity: Joi.number().positive().required(),
    unitPrice: Joi.number().positive().required(),
    total: Joi.number().positive().required()
  })).min(1).required(),
  subtotal: Joi.number().positive().required(),
  taxAmount: Joi.number().min(0).default(0),
  total: Joi.number().positive().required(),
  currency: Joi.string().length(3).default('USD'),
  dueDate: Joi.date().iso().required(),
  metadata: Joi.object().default({})
});

const paymentSchema = Joi.object({
  invoiceId: Joi.string().required(),
  amount: Joi.number().positive().required(),
  method: Joi.string().valid('credit_card', 'bank_transfer', 'paypal', 'stripe').required(),
  transactionId: Joi.string().required(),
  metadata: Joi.object().default({})
});

const subscriptionSchema = Joi.object({
  customerId: Joi.string().required(),
  planId: Joi.string().required(),
  billingCycle: Joi.string().valid('monthly', 'quarterly', 'annually').required(),
  amount: Joi.number().positive().required(),
  currency: Joi.string().length(3).default('USD'),
  startDate: Joi.date().iso().default(() => new Date()),
  metadata: Joi.object().default({})
});

// Utility functions
function calculateTax(amount, taxRate = 0.1) {
  return new Decimal(amount).mul(taxRate).toNumber();
}

function generateInvoiceNumber() {
  const timestamp = Date.now().toString().slice(-6);
  const random = Math.random().toString(36).substr(2, 4).toUpperCase();
  return `INV-${timestamp}-${random}`;
}

// Invoice Management
app.post('/invoices', async (req, res) => {
  try {
    const { error, value } = invoiceSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }

    const id = uuidv4();
    const invoiceNumber = generateInvoiceNumber();
    
    const invoice = {
      id,
      invoiceNumber,
      ...value,
      status: 'unpaid',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    invoices[id] = invoice;
    
    logger.info(`Invoice created: ${invoiceNumber}`, { 
      invoiceId: id, 
      customerId: value.customerId,
      total: value.total 
    });

    res.status(201).json(invoice);
  } catch (error) {
    logger.error('Error creating invoice:', error);
    res.status(500).json({ error: 'Failed to create invoice' });
  }
});

app.get('/invoices/:id', (req, res) => {
  try {
    const invoice = invoices[req.params.id];
    if (!invoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    // Get related payments
    const relatedPayments = Object.values(payments)
      .filter(payment => payment.invoiceId === req.params.id);

    res.json({
      ...invoice,
      payments: relatedPayments
    });
  } catch (error) {
    logger.error('Error retrieving invoice:', error);
    res.status(500).json({ error: 'Failed to retrieve invoice' });
  }
});

app.get('/invoices', (req, res) => {
  try {
    const { customerId, status, page = 1, limit = 20 } = req.query;
    let filteredInvoices = Object.values(invoices);

    // Apply filters
    if (customerId) {
      filteredInvoices = filteredInvoices.filter(inv => inv.customerId === customerId);
    }
    if (status) {
      filteredInvoices = filteredInvoices.filter(inv => inv.status === status);
    }

    // Pagination
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + parseInt(limit);
    const paginatedInvoices = filteredInvoices.slice(startIndex, endIndex);

    res.json({
      invoices: paginatedInvoices,
      pagination: {
        total: filteredInvoices.length,
        page: parseInt(page),
        limit: parseInt(limit),
        totalPages: Math.ceil(filteredInvoices.length / limit)
      }
    });
  } catch (error) {
    logger.error('Error listing invoices:', error);
    res.status(500).json({ error: 'Failed to list invoices' });
  }
});

// Payment Processing
app.post('/payments', async (req, res) => {
  try {
    const { error, value } = paymentSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }

    const invoice = invoices[value.invoiceId];
    if (!invoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    if (invoice.status === 'paid') {
      return res.status(400).json({ error: 'Invoice already paid' });
    }

    const paymentId = uuidv4();
    const payment = {
      id: paymentId,
      ...value,
      status: 'completed',
      paidAt: new Date().toISOString(),
      processingFee: new Decimal(value.amount).mul(0.029).toNumber() // 2.9% processing fee
    };

    payments[paymentId] = payment;

    // Update invoice status
    invoice.status = 'paid';
    invoice.paidAt = payment.paidAt;
    invoice.updatedAt = new Date().toISOString();

    logger.info(`Payment processed: ${paymentId}`, {
      invoiceId: value.invoiceId,
      amount: value.amount,
      method: value.method
    });

    res.status(201).json({
      payment,
      invoice
    });
  } catch (error) {
    logger.error('Error processing payment:', error);
    res.status(500).json({ error: 'Failed to process payment' });
  }
});

app.get('/payments', (req, res) => {
  try {
    const { customerId, invoiceId, page = 1, limit = 20 } = req.query;
    let filteredPayments = Object.values(payments);

    // Apply filters
    if (invoiceId) {
      filteredPayments = filteredPayments.filter(pay => pay.invoiceId === invoiceId);
    }

    // Pagination
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + parseInt(limit);
    const paginatedPayments = filteredPayments.slice(startIndex, endIndex);

    res.json({
      payments: paginatedPayments,
      pagination: {
        total: filteredPayments.length,
        page: parseInt(page),
        limit: parseInt(limit),
        totalPages: Math.ceil(filteredPayments.length / limit)
      }
    });
  } catch (error) {
    logger.error('Error listing payments:', error);
    res.status(500).json({ error: 'Failed to list payments' });
  }
});

// Subscription Management
app.post('/subscriptions', async (req, res) => {
  try {
    const { error, value } = subscriptionSchema.validate(req.body);
    if (error) {
      return res.status(400).json({ error: error.details[0].message });
    }

    const id = uuidv4();
    const subscription = {
      id,
      ...value,
      status: 'active',
      nextBillingDate: moment(value.startDate).add(1, value.billingCycle.replace('ly', '')).toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    subscriptions[id] = subscription;

    logger.info(`Subscription created: ${id}`, {
      customerId: value.customerId,
      planId: value.planId,
      amount: value.amount
    });

    res.status(201).json(subscription);
  } catch (error) {
    logger.error('Error creating subscription:', error);
    res.status(500).json({ error: 'Failed to create subscription' });
  }
});

app.get('/subscriptions', (req, res) => {
  try {
    const { customerId, status } = req.query;
    let filteredSubscriptions = Object.values(subscriptions);

    if (customerId) {
      filteredSubscriptions = filteredSubscriptions.filter(sub => sub.customerId === customerId);
    }
    if (status) {
      filteredSubscriptions = filteredSubscriptions.filter(sub => sub.status === status);
    }

    res.json({ subscriptions: filteredSubscriptions });
  } catch (error) {
    logger.error('Error listing subscriptions:', error);
    res.status(500).json({ error: 'Failed to list subscriptions' });
  }
});

// Revenue Analytics
app.get('/analytics/revenue', (req, res) => {
  try {
    const cacheKey = 'revenue_analytics';
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }

    const totalRevenue = Object.values(payments)
      .reduce((sum, payment) => new Decimal(sum).add(payment.amount).toNumber(), 0);

    const monthlyRevenue = Object.values(payments)
      .filter(payment => moment(payment.paidAt).isSame(moment(), 'month'))
      .reduce((sum, payment) => new Decimal(sum).add(payment.amount).toNumber(), 0);

    const totalInvoices = Object.keys(invoices).length;
    const paidInvoices = Object.values(invoices).filter(inv => inv.status === 'paid').length;
    const pendingInvoices = totalInvoices - paidInvoices;

    const activeSubscriptions = Object.values(subscriptions)
      .filter(sub => sub.status === 'active').length;

    const monthlyRecurringRevenue = Object.values(subscriptions)
      .filter(sub => sub.status === 'active' && sub.billingCycle === 'monthly')
      .reduce((sum, sub) => new Decimal(sum).add(sub.amount).toNumber(), 0);

    const analytics = {
      totalRevenue,
      monthlyRevenue,
      totalInvoices,
      paidInvoices,
      pendingInvoices,
      paymentRate: totalInvoices > 0 ? (paidInvoices / totalInvoices * 100).toFixed(2) : 0,
      activeSubscriptions,
      monthlyRecurringRevenue,
      timestamp: new Date().toISOString()
    };

    cache.set(cacheKey, analytics);
    res.json(analytics);
  } catch (error) {
    logger.error('Error generating revenue analytics:', error);
    res.status(500).json({ error: 'Failed to generate analytics' });
  }
});

// Health and status endpoints
app.get('/health', (req, res) => {
  res.json({
    status: 'OK',
    service: 'billing-engine',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

app.get('/metrics', (req, res) => {
  try {
    const cacheStats = cache.getStats();
    
    res.json({
      service: 'billing-engine',
      version: '1.0.0',
      uptime: process.uptime(),
      memory: process.memoryUsage(),
      invoiceCount: Object.keys(invoices).length,
      paymentCount: Object.keys(payments).length,
      subscriptionCount: Object.keys(subscriptions).length,
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
  logger.info(`Billing Engine running on port ${PORT}`);
  console.log(`Billing Engine running on port ${PORT}`);
});

module.exports = app; // For testing