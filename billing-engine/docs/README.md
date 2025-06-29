# Billing Engine Documentation

## Overview

The Billing Engine is a high-performance Node.js microservice that provides comprehensive billing, invoicing, and financial processing capabilities for the Social E-commerce Ecosystem. It handles complex billing scenarios, subscription management, invoice generation, payment processing integration, and financial reporting.

## Components

### Core Components
- **BillingManager**: Central billing processing engine
- **InvoiceGenerator**: Invoice creation and formatting
- **SubscriptionManager**: Recurring billing and subscription management
- **PaymentProcessor**: Payment processing and reconciliation
- **TaxCalculator**: Tax computation and compliance

### Financial Components
- **RevenueRecognition**: Revenue recognition and deferred billing
- **CreditManager**: Credit notes and refund processing
- **DiscountEngine**: Promotional pricing and discount application
- **CurrencyConverter**: Multi-currency support and exchange rates
- **AuditLogger**: Financial audit trail and compliance logging

### Integration Components
- **PaymentGatewayClient**: External payment provider integration
- **TaxServiceClient**: Third-party tax service integration
- **NotificationClient**: Billing notification and alerts
- **ReportingEngine**: Financial reporting and analytics
- **WebhookManager**: Event-driven billing notifications

## Getting Started

To use the Billing Engine, follow these steps:

1. Configure the billing service with database and payment provider settings
2. Set up subscription plans and pricing models
3. Configure tax rules and currency settings
4. Initialize payment gateway integrations
5. Set up billing cycles and notification preferences

## Examples

### Configuring the Billing Engine

```javascript
const { BillingEngine } = require('@exalt/billing-engine');

const billingEngine = new BillingEngine({
  database: {
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    database: process.env.DB_NAME,
    username: process.env.DB_USER,
    password: process.env.DB_PASSWORD
  },
  redis: {
    host: process.env.REDIS_HOST,
    port: process.env.REDIS_PORT
  },
  paymentGateways: {
    stripe: {
      secretKey: process.env.STRIPE_SECRET_KEY,
      publishableKey: process.env.STRIPE_PUBLISHABLE_KEY,
      webhookSecret: process.env.STRIPE_WEBHOOK_SECRET
    },
    paypal: {
      clientId: process.env.PAYPAL_CLIENT_ID,
      clientSecret: process.env.PAYPAL_CLIENT_SECRET,
      environment: process.env.PAYPAL_ENVIRONMENT
    }
  },
  taxation: {
    provider: 'taxjar',
    apiKey: process.env.TAXJAR_API_KEY,
    nexusAddresses: [
      { country: 'US', state: 'CA' },
      { country: 'US', state: 'NY' }
    ]
  }
});

await billingEngine.initialize();
```

### Creating a Subscription Plan

```javascript
const { SubscriptionManager } = require('@exalt/billing-engine');

const subscriptionManager = new SubscriptionManager(billingEngine);

const basicPlan = await subscriptionManager.createPlan({
  id: 'basic-monthly',
  name: 'Basic Plan',
  description: 'Basic subscription with core features',
  currency: 'USD',
  amount: 29.99,
  interval: 'month',
  intervalCount: 1,
  trialPeriodDays: 14,
  features: [
    'up-to-1000-products',
    'basic-analytics',
    'email-support'
  ],
  metadata: {
    category: 'starter',
    maxUsers: 5
  }
});

const premiumPlan = await subscriptionManager.createPlan({
  id: 'premium-monthly',
  name: 'Premium Plan',
  description: 'Premium subscription with advanced features',
  currency: 'USD',
  amount: 99.99,
  interval: 'month',
  intervalCount: 1,
  trialPeriodDays: 30,
  features: [
    'unlimited-products',
    'advanced-analytics',
    'priority-support',
    'api-access'
  ],
  metadata: {
    category: 'professional',
    maxUsers: 50
  }
});
```

### Processing Customer Subscriptions

```javascript
const { CustomerManager } = require('@exalt/billing-engine');

const customerManager = new CustomerManager(billingEngine);

// Create customer
const customer = await customerManager.createCustomer({
  email: 'customer@exalt.com',
  name: 'John Doe',
  companyName: 'Exalt Store',
  billingAddress: {
    line1: '123 Main St',
    line2: 'Suite 100',
    city: 'San Francisco',
    state: 'CA',
    postalCode: '94105',
    country: 'US'
  },
  taxId: '12-3456789',
  metadata: {
    source: 'web-signup',
    marketingChannel: 'google-ads'
  }
});

// Subscribe customer to plan
const subscription = await subscriptionManager.createSubscription({
  customerId: customer.id,
  planId: 'premium-monthly',
  paymentMethodId: 'pm_1234567890',
  startDate: new Date(),
  prorationBehavior: 'create_prorations',
  addons: [
    {
      id: 'extra-storage',
      quantity: 5,
      amount: 10.00
    }
  ],
  coupon: 'WELCOME25',
  metadata: {
    salesRep: 'jane.smith@exalt.com',
    contractLength: '12-months'
  }
});
```

### Invoice Generation and Processing

```javascript
const { InvoiceGenerator } = require('@exalt/billing-engine');

const invoiceGenerator = new InvoiceGenerator(billingEngine);

// Generate invoice for subscription
const invoice = await invoiceGenerator.generateInvoice({
  customerId: customer.id,
  subscriptionId: subscription.id,
  billingPeriod: {
    start: new Date('2024-06-01'),
    end: new Date('2024-06-30')
  },
  lineItems: [
    {
      description: 'Premium Plan - Monthly',
      amount: 99.99,
      quantity: 1,
      taxable: true,
      category: 'subscription'
    },
    {
      description: 'Extra Storage (5 GB)',
      amount: 50.00,
      quantity: 1,
      taxable: true,
      category: 'addon'
    }
  ],
  discounts: [
    {
      type: 'percentage',
      value: 25,
      description: 'Welcome Discount',
      couponCode: 'WELCOME25'
    }
  ]
});

// Process payment for invoice
const paymentResult = await invoiceGenerator.processPayment({
  invoiceId: invoice.id,
  paymentMethodId: 'pm_1234567890',
  currency: 'USD',
  amount: invoice.totalAmount,
  description: `Payment for Invoice ${invoice.number}`,
  metadata: {
    invoiceNumber: invoice.number,
    customerId: customer.id
  }
});

if (paymentResult.success) {
  await invoiceGenerator.markInvoicePaid({
    invoiceId: invoice.id,
    paymentId: paymentResult.paymentId,
    paidAt: new Date(),
    amountPaid: paymentResult.amount
  });
  
  // Send invoice to customer
  await invoiceGenerator.sendInvoice({
    invoiceId: invoice.id,
    recipientEmail: customer.email,
    template: 'invoice-receipt',
    attachPdf: true
  });
}
```

### Usage-Based Billing

```javascript
const { UsageBillingManager } = require('@exalt/billing-engine');

const usageBillingManager = new UsageBillingManager(billingEngine);

// Define usage-based pricing model
const usagePlan = await usageBillingManager.createUsagePlan({
  id: 'api-calls-tier',
  name: 'API Usage Billing',
  currency: 'USD',
  billingScheme: 'per_unit',
  tiers: [
    {
      upTo: 10000,
      unitAmount: 0.01,
      flatAmount: 0
    },
    {
      upTo: 100000,
      unitAmount: 0.008,
      flatAmount: 0
    },
    {
      upTo: null, // Unlimited
      unitAmount: 0.005,
      flatAmount: 0
    }
  ],
  aggregateUsage: 'sum',
  usageType: 'metered'
});

// Record usage events
await usageBillingManager.recordUsage({
  subscriptionId: subscription.id,
  usagePlanId: usagePlan.id,
  quantity: 1523,
  timestamp: new Date(),
  idempotencyKey: 'usage-2024-06-24-001',
  metadata: {
    endpoint: '/api/v1/products',
    customerId: customer.id,
    apiKey: 'ak_1234567890'
  }
});

// Generate usage invoice at end of billing period
const usageInvoice = await usageBillingManager.generateUsageInvoice({
  subscriptionId: subscription.id,
  billingPeriod: {
    start: new Date('2024-06-01'),
    end: new Date('2024-06-30')
  }
});
```

### Tax Calculation and Compliance

```javascript
const { TaxCalculator } = require('@exalt/billing-engine');

const taxCalculator = new TaxCalculator(billingEngine);

// Calculate taxes for transaction
const taxCalculation = await taxCalculator.calculateTax({
  fromAddress: {
    country: 'US',
    state: 'CA',
    city: 'San Francisco',
    zip: '94105'
  },
  toAddress: {
    country: 'US',
    state: 'NY',
    city: 'New York',
    zip: '10001'
  },
  lineItems: [
    {
      id: 'premium-plan',
      quantity: 1,
      unitPrice: 99.99,
      productTaxCode: 'SW056000', // Software/SaaS
      description: 'Premium Plan Subscription'
    }
  ],
  customerId: customer.id,
  customerTaxId: customer.taxId
});

// Apply calculated taxes to invoice
await invoiceGenerator.applyTaxes({
  invoiceId: invoice.id,
  taxes: taxCalculation.taxes,
  totalTaxAmount: taxCalculation.totalTax,
  taxBreakdown: taxCalculation.breakdown
});
```

### Revenue Recognition and Reporting

```javascript
const { RevenueRecognition } = require('@exalt/billing-engine');

const revenueRecognition = new RevenueRecognition(billingEngine);

// Set up revenue recognition schedule
const revenueSchedule = await revenueRecognition.createSchedule({
  invoiceId: invoice.id,
  totalAmount: invoice.totalAmount,
  recognitionMethod: 'straight_line',
  startDate: new Date('2024-06-01'),
  endDate: new Date('2024-06-30'),
  recognitionRules: {
    subscriptions: 'monthly_recognition',
    oneTime: 'immediate_recognition',
    setup: 'immediate_recognition'
  }
});

// Generate revenue recognition entries
const revenueEntries = await revenueRecognition.generateEntries({
  scheduleId: revenueSchedule.id,
  period: {
    start: new Date('2024-06-01'),
    end: new Date('2024-06-30')
  }
});

// Export for accounting system
const revenueReport = await revenueRecognition.generateReport({
  startDate: new Date('2024-06-01'),
  endDate: new Date('2024-06-30'),
  format: 'csv',
  groupBy: ['plan', 'customer', 'region'],
  includeDeferred: true
});
```

### Payment Processing and Webhooks

```javascript
const { PaymentProcessor, WebhookManager } = require('@exalt/billing-engine');

const paymentProcessor = new PaymentProcessor(billingEngine);
const webhookManager = new WebhookManager(billingEngine);

// Handle payment webhooks
webhookManager.on('payment.succeeded', async (event) => {
  const payment = event.data.object;
  
  await paymentProcessor.handleSuccessfulPayment({
    paymentId: payment.id,
    amount: payment.amount,
    currency: payment.currency,
    customerId: payment.customer,
    metadata: payment.metadata
  });
  
  // Update invoice status
  if (payment.invoice) {
    await invoiceGenerator.markInvoicePaid({
      invoiceId: payment.invoice,
      paymentId: payment.id,
      paidAt: new Date(payment.created * 1000)
    });
  }
});

webhookManager.on('payment.failed', async (event) => {
  const payment = event.data.object;
  
  await paymentProcessor.handleFailedPayment({
    paymentId: payment.id,
    customerId: payment.customer,
    failureReason: payment.failure_reason,
    failureCode: payment.failure_code
  });
  
  // Retry payment with exponential backoff
  await paymentProcessor.scheduleRetry({
    customerId: payment.customer,
    paymentMethodId: payment.payment_method,
    amount: payment.amount,
    currency: payment.currency,
    retryAttempt: 1,
    maxRetries: 3
  });
});

// Process refunds
const refund = await paymentProcessor.createRefund({
  paymentId: 'pi_1234567890',
  amount: 99.99,
  reason: 'requested_by_customer',
  metadata: {
    refundReason: 'Not satisfied with service',
    requestedBy: 'customer@exalt.com'
  }
});
```

### Financial Reporting and Analytics

```javascript
const { ReportingEngine } = require('@exalt/billing-engine');

const reportingEngine = new ReportingEngine(billingEngine);

// Generate MRR (Monthly Recurring Revenue) report
const mrrReport = await reportingEngine.generateMRRReport({
  startDate: new Date('2024-01-01'),
  endDate: new Date('2024-06-30'),
  currency: 'USD',
  groupBy: ['plan', 'region'],
  includeChurn: true,
  includeExpansion: true
});

// Generate churn analysis
const churnReport = await reportingEngine.generateChurnReport({
  period: 'monthly',
  startDate: new Date('2024-01-01'),
  endDate: new Date('2024-06-30'),
  cohortAnalysis: true,
  segmentBy: ['plan', 'signupSource', 'companySize']
});

// Generate revenue waterfall
const revenueWaterfall = await reportingEngine.generateRevenueWaterfall({
  period: 'monthly',
  year: 2024,
  currency: 'USD',
  breakdown: {
    newBusiness: true,
    expansion: true,
    contraction: true,
    churn: true
  }
});

// Export reports
await reportingEngine.exportReport({
  reportId: mrrReport.id,
  format: 'xlsx',
  destination: 's3://exalt-reports/billing/',
  filename: `mrr-report-${new Date().toISOString().slice(0, 10)}.xlsx`
});
```

## API Reference

### Core Billing API

#### BillingEngine
- `BillingEngine(config)`: Initialize billing engine with configuration
- `initialize()`: Initialize database connections and external integrations
- `shutdown()`: Gracefully shutdown all connections
- `getStatus()`: Get engine status and health information
- `getMetrics()`: Get billing metrics and statistics

#### CustomerManager
- `createCustomer(customerData)`: Create new customer record
- `updateCustomer(customerId, updateData)`: Update customer information
- `getCustomer(customerId)`: Retrieve customer details
- `deleteCustomer(customerId)`: Delete customer and associated data
- `listCustomers(filters, pagination)`: List customers with filtering
- `searchCustomers(query)`: Search customers by various criteria

#### SubscriptionManager
- `createPlan(planData)`: Create subscription plan
- `updatePlan(planId, updateData)`: Update existing plan
- `createSubscription(subscriptionData)`: Create customer subscription
- `updateSubscription(subscriptionId, updateData)`: Update subscription
- `cancelSubscription(subscriptionId, options)`: Cancel subscription
- `pauseSubscription(subscriptionId, options)`: Pause subscription
- `resumeSubscription(subscriptionId)`: Resume paused subscription

### Invoice and Payment API

#### InvoiceGenerator
- `generateInvoice(invoiceData)`: Generate new invoice
- `updateInvoice(invoiceId, updateData)`: Update invoice details
- `finalizeInvoice(invoiceId)`: Finalize draft invoice
- `sendInvoice(invoiceId, options)`: Send invoice to customer
- `markInvoicePaid(invoiceId, paymentData)`: Mark invoice as paid
- `voidInvoice(invoiceId, reason)`: Void invoice
- `generatePDF(invoiceId, options)`: Generate PDF invoice

#### PaymentProcessor
- `processPayment(paymentData)`: Process payment transaction
- `createRefund(refundData)`: Create payment refund
- `capturePayment(paymentId)`: Capture authorized payment
- `getPaymentStatus(paymentId)`: Get payment status
- `listPayments(filters, pagination)`: List payments with filtering
- `handleWebhook(webhookData)`: Process payment webhook events

## Best Practices

1. **Error Handling**: Implement comprehensive error handling for payment failures
2. **Idempotency**: Use idempotency keys for critical billing operations
3. **Audit Logging**: Maintain detailed audit trails for all financial transactions
4. **Data Validation**: Validate all billing data before processing
5. **Security**: Encrypt sensitive financial data and use secure payment methods
6. **Testing**: Thoroughly test billing logic with various scenarios
7. **Monitoring**: Monitor billing metrics and set up alerts for anomalies

## Related Documentation

- [API Specification](../api-docs/openapi.yaml)
- [Architecture Documentation](./architecture/README.md)
- [Setup Guide](./setup/README.md)
- [Operations Guide](./operations/README.md)