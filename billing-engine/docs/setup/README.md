# Setup Guide - Billing Engine

## Overview

This comprehensive setup guide covers the installation, configuration, and deployment of the Billing Engine service for the Social E-commerce Ecosystem. It includes local development setup, Docker deployment, Kubernetes configuration, and production readiness steps.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Database Setup](#database-setup)
4. [External Service Configuration](#external-service-configuration)
5. [Docker Setup](#docker-setup)
6. [Kubernetes Setup](#kubernetes-setup)
7. [Payment Gateway Integration](#payment-gateway-integration)
8. [Testing and Verification](#testing-and-verification)
9. [Production Deployment](#production-deployment)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: Minimum 10GB free space
- **Network**: Stable internet connection for external services

### Required Software

#### Development Environment

```bash
# Node.js Runtime
Node.js 18+ (LTS recommended)
npm 8+ or yarn 1.22+

# Database
PostgreSQL 13+
Redis 6.0+

# Message Queue
Apache Kafka 3.0+ (optional for production)
RabbitMQ 3.10+ (alternative)

# Development Tools
Git 2.30+
Docker Desktop 4.0+ (optional)
VS Code or preferred IDE
```

#### External Services

```bash
# Payment Gateways (at least one)
- Stripe Account
- PayPal Business Account
- Square Developer Account

# Tax Services (optional)
- TaxJar API Access
- Avalara AvaTax

# Communication
- SendGrid/AWS SES for emails
- Twilio for SMS (optional)
```

### Installation Instructions

#### Node.js Installation

**Windows:**
```powershell
# Using Chocolatey
choco install nodejs-lts

# Or download from nodejs.org
# Verify installation
node --version
npm --version
```

**macOS:**
```bash
# Using Homebrew
brew install node@18

# Or using nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18

# Verify installation
node --version
npm --version
```

**Linux:**
```bash
# Using NodeSource repository
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Verify installation
node --version
npm --version
```

#### PostgreSQL Setup

**Windows:**
```powershell
# Using Chocolatey
choco install postgresql13

# Start PostgreSQL
net start postgresql-x64-13
```

**macOS:**
```bash
# Using Homebrew
brew install postgresql@13
brew services start postgresql@13

# Create database
createuser -s billing_user
createdb -O billing_user billing_db
```

**Linux:**
```bash
# Install PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Create database and user
sudo -u postgres createuser --interactive billing_user
sudo -u postgres createdb -O billing_user billing_db
```

#### Redis Setup

**All Platforms with Docker:**
```bash
# Run Redis container
docker run -d --name redis-billing -p 6379:6379 redis:6-alpine

# Or install natively
# macOS: brew install redis
# Linux: sudo apt install redis-server
# Windows: Download from GitHub releases
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/billing-engine
```

### 2. Install Dependencies

```bash
# Install Node.js dependencies
npm install

# Or using yarn
yarn install

# Install development dependencies
npm install --save-dev
```

### 3. Environment Configuration

```bash
# Copy environment template
cp .env.template .env

# Edit configuration
nano .env  # or use your preferred editor
```

Required environment variables:

```bash
# Service Configuration
NODE_ENV=development
PORT=3401
SERVICE_NAME=billing-engine
LOG_LEVEL=debug

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=billing_db
DB_USER=billing_user
DB_PASSWORD=secure_password
DB_SSL=false
DB_POOL_MIN=2
DB_POOL_MAX=10

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DB=0
REDIS_KEY_PREFIX=billing:

# Kafka Configuration (optional for development)
KAFKA_ENABLED=false
KAFKA_BROKERS=localhost:9092
KAFKA_CLIENT_ID=billing-engine
KAFKA_GROUP_ID=billing-engine-group

# JWT Configuration (for authentication)
JWT_PUBLIC_KEY=your-public-key-here
JWT_ISSUER=auth-service
JWT_AUDIENCE=billing-engine

# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# PayPal Configuration
PAYPAL_CLIENT_ID=your-paypal-client-id
PAYPAL_CLIENT_SECRET=your-paypal-client-secret
PAYPAL_ENVIRONMENT=sandbox
PAYPAL_WEBHOOK_ID=your-webhook-id

# Tax Service Configuration (TaxJar)
TAXJAR_API_KEY=your-taxjar-api-key
TAXJAR_SANDBOX=true

# Email Service (SendGrid)
SENDGRID_API_KEY=your-sendgrid-api-key
EMAIL_FROM=billing@exalt.com
EMAIL_REPLY_TO=support@exalt.com

# File Storage (AWS S3)
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_REGION=us-east-1
S3_BUCKET_NAME=exalt-billing-documents

# Service Discovery
EUREKA_ENABLED=true
EUREKA_URL=http://localhost:8761/eureka
EUREKA_HOSTNAME=localhost

# Monitoring
METRICS_ENABLED=true
HEALTH_CHECK_INTERVAL=30000
```

### 4. Database Initialization

```bash
# Run database migrations
npm run db:migrate

# Seed initial data (optional)
npm run db:seed

# Verify database setup
npm run db:verify
```

### 5. Start Development Server

```bash
# Start with nodemon for hot-reload
npm run dev

# Or start normally
npm start

# Start with debugging
npm run debug
```

## Database Setup

### 1. Schema Creation

```sql
-- Create billing schema
CREATE SCHEMA IF NOT EXISTS billing;

-- Set search path
SET search_path TO billing, public;

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
```

### 2. Run Migrations

The service uses database migrations for schema management:

```bash
# Create new migration
npm run migration:create -- --name add_customer_table

# Run pending migrations
npm run migration:up

# Rollback last migration
npm run migration:down

# Check migration status
npm run migration:status
```

### 3. Database Indexes

```sql
-- Performance indexes
CREATE INDEX CONCURRENTLY idx_customers_email ON customers(email);
CREATE INDEX CONCURRENTLY idx_subscriptions_customer_status ON subscriptions(customer_id, status);
CREATE INDEX CONCURRENTLY idx_invoices_due_unpaid ON invoices(due_date) WHERE status = 'unpaid';
CREATE INDEX CONCURRENTLY idx_payments_created ON payments(created_at DESC);

-- Full-text search indexes
CREATE INDEX CONCURRENTLY idx_customers_search ON customers USING gin(to_tsvector('english', name || ' ' || email));
```

## External Service Configuration

### 1. Stripe Setup

```javascript
// Initialize Stripe
const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);

// Configure webhook endpoint
// In Stripe Dashboard: https://dashboard.stripe.com/webhooks
// Endpoint URL: https://your-domain.com/webhooks/stripe
// Events to listen:
// - payment_intent.succeeded
// - payment_intent.payment_failed
// - customer.subscription.created
// - customer.subscription.updated
// - customer.subscription.deleted
// - invoice.payment_succeeded
// - invoice.payment_failed
```

### 2. PayPal Setup

```javascript
// PayPal SDK configuration
const paypal = require('@paypal/checkout-server-sdk');

const environment = process.env.PAYPAL_ENVIRONMENT === 'production'
  ? new paypal.core.LiveEnvironment(
      process.env.PAYPAL_CLIENT_ID,
      process.env.PAYPAL_CLIENT_SECRET
    )
  : new paypal.core.SandboxEnvironment(
      process.env.PAYPAL_CLIENT_ID,
      process.env.PAYPAL_CLIENT_SECRET
    );

const client = new paypal.core.PayPalHttpClient(environment);
```

### 3. Tax Service Setup

```javascript
// TaxJar configuration
const Taxjar = require('taxjar');

const taxjar = new Taxjar({
  apiKey: process.env.TAXJAR_API_KEY,
  apiUrl: process.env.TAXJAR_SANDBOX === 'true' 
    ? Taxjar.SANDBOX_API_URL 
    : Taxjar.DEFAULT_API_URL
});

// Configure nexus addresses
const nexusAddresses = [
  {
    country: 'US',
    state: 'CA',
    zip: '90210'
  },
  {
    country: 'US',
    state: 'NY',
    zip: '10001'
  }
];
```

## Docker Setup

### 1. Development Dockerfile

```dockerfile
FROM node:18-alpine

# Install dependencies for native modules
RUN apk add --no-cache python3 make g++

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy application files
COPY . .

# Expose port
EXPOSE 3401

# Start application
CMD ["npm", "run", "dev"]
```

### 2. Production Dockerfile

```dockerfile
# Multi-stage build
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

FROM node:18-alpine

# Add non-root user
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nodejs -u 1001

WORKDIR /app

# Copy from builder
COPY --from=builder --chown=nodejs:nodejs /app/node_modules ./node_modules
COPY --chown=nodejs:nodejs . .

# Switch to non-root user
USER nodejs

EXPOSE 3401

CMD ["node", "src/index.js"]
```

### 3. Docker Compose Configuration

```yaml
version: '3.8'

services:
  billing-engine:
    build: .
    ports:
      - "3401:3401"
    environment:
      - NODE_ENV=development
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BROKERS=kafka:9092
    depends_on:
      - postgres
      - redis
      - kafka
    volumes:
      - ./src:/app/src
      - ./node_modules:/app/node_modules
    networks:
      - billing-network

  postgres:
    image: postgres:13-alpine
    environment:
      - POSTGRES_DB=billing_db
      - POSTGRES_USER=billing_user
      - POSTGRES_PASSWORD=billing_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
    networks:
      - billing-network

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - billing-network

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    networks:
      - billing-network

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    ports:
      - "2181:2181"
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
      - ZOOKEEPER_TICK_TIME=2000
    networks:
      - billing-network

volumes:
  postgres_data:
  redis_data:

networks:
  billing-network:
    driver: bridge
```

### 4. Running with Docker

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f billing-engine

# Run database migrations
docker-compose exec billing-engine npm run db:migrate

# Stop all services
docker-compose down

# Clean up volumes
docker-compose down -v
```

## Kubernetes Setup

### 1. ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: billing-engine-config
  namespace: billing
data:
  NODE_ENV: "production"
  PORT: "3401"
  SERVICE_NAME: "billing-engine"
  LOG_LEVEL: "info"
  DB_HOST: "postgres-service"
  DB_PORT: "5432"
  DB_NAME: "billing_db"
  REDIS_HOST: "redis-service"
  REDIS_PORT: "6379"
  KAFKA_BROKERS: "kafka-service:9092"
```

### 2. Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: billing-engine-secret
  namespace: billing
type: Opaque
stringData:
  DB_PASSWORD: "your-db-password"
  STRIPE_SECRET_KEY: "your-stripe-secret"
  PAYPAL_CLIENT_SECRET: "your-paypal-secret"
  TAXJAR_API_KEY: "your-taxjar-key"
  SENDGRID_API_KEY: "your-sendgrid-key"
  AWS_SECRET_ACCESS_KEY: "your-aws-secret"
```

### 3. Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: billing-engine
  namespace: billing
  labels:
    app: billing-engine
spec:
  replicas: 3
  selector:
    matchLabels:
      app: billing-engine
  template:
    metadata:
      labels:
        app: billing-engine
    spec:
      containers:
      - name: billing-engine
        image: billing-engine:latest
        ports:
        - containerPort: 3401
          name: http
        envFrom:
        - configMapRef:
            name: billing-engine-config
        - secretRef:
            name: billing-engine-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health/live
            port: 3401
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 3401
          initialDelaySeconds: 5
          periodSeconds: 5
        volumeMounts:
        - name: invoice-storage
          mountPath: /app/invoices
      volumes:
      - name: invoice-storage
        persistentVolumeClaim:
          claimName: invoice-storage-pvc
```

### 4. Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: billing-engine-service
  namespace: billing
  labels:
    app: billing-engine
spec:
  type: ClusterIP
  ports:
  - port: 3401
    targetPort: 3401
    protocol: TCP
    name: http
  selector:
    app: billing-engine
```

### 5. Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: billing-engine-hpa
  namespace: billing
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: billing-engine
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 6. Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace billing

# Apply configurations
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n billing -l app=billing-engine

# View logs
kubectl logs -f deployment/billing-engine -n billing

# Port forward for testing
kubectl port-forward service/billing-engine-service 3401:3401 -n billing
```

## Payment Gateway Integration

### 1. Stripe Integration

```javascript
// src/gateways/stripe.js
const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);

class StripeGateway {
  async createCustomer(customerData) {
    return await stripe.customers.create({
      email: customerData.email,
      name: customerData.name,
      metadata: {
        externalId: customerData.id
      }
    });
  }

  async createPaymentIntent(amount, currency, metadata) {
    return await stripe.paymentIntents.create({
      amount: Math.round(amount * 100), // Convert to cents
      currency: currency.toLowerCase(),
      metadata: metadata
    });
  }

  async createSubscription(customerId, priceId, options = {}) {
    return await stripe.subscriptions.create({
      customer: customerId,
      items: [{ price: priceId }],
      ...options
    });
  }
}
```

### 2. Webhook Setup

```javascript
// src/webhooks/stripe.js
const express = require('express');
const router = express.Router();

router.post('/stripe', express.raw({ type: 'application/json' }), async (req, res) => {
  const sig = req.headers['stripe-signature'];
  
  try {
    const event = stripe.webhooks.constructEvent(
      req.body,
      sig,
      process.env.STRIPE_WEBHOOK_SECRET
    );
    
    // Handle events
    switch (event.type) {
      case 'payment_intent.succeeded':
        await handlePaymentSuccess(event.data.object);
        break;
      case 'payment_intent.payment_failed':
        await handlePaymentFailure(event.data.object);
        break;
      // Add more event handlers
    }
    
    res.json({ received: true });
  } catch (err) {
    console.error('Webhook error:', err.message);
    res.status(400).send(`Webhook Error: ${err.message}`);
  }
});
```

## Testing and Verification

### 1. Unit Tests

```bash
# Run all tests
npm test

# Run with coverage
npm run test:coverage

# Run specific test file
npm test -- src/services/billing.test.js

# Run in watch mode
npm run test:watch
```

### 2. Integration Tests

```bash
# Run integration tests
npm run test:integration

# Test payment gateway integration
npm run test:payment-gateways

# Test database operations
npm run test:database
```

### 3. Load Testing

```bash
# Install artillery
npm install -g artillery

# Run load test
artillery run tests/load/billing-load-test.yml

# Quick test
artillery quick --count 100 --num 50 http://localhost:3401/api/v1/health
```

### 4. API Testing

```bash
# Health check
curl http://localhost:3401/health

# Create customer
curl -X POST http://localhost:3401/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "email": "test@example.com",
    "name": "Test Customer",
    "currency": "USD"
  }'

# Create subscription
curl -X POST http://localhost:3401/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "customerId": "cus_123",
    "planId": "plan_basic",
    "paymentMethodId": "pm_123"
  }'
```

## Production Deployment

### 1. Pre-deployment Checklist

- [ ] All tests passing
- [ ] Security scan completed
- [ ] Performance testing done
- [ ] Database migrations tested
- [ ] SSL certificates configured
- [ ] Monitoring alerts set up
- [ ] Backup procedures in place
- [ ] Rollback plan documented

### 2. Environment Variables

```bash
# Production-specific settings
NODE_ENV=production
LOG_LEVEL=warn
DB_SSL=true
DB_POOL_MIN=10
DB_POOL_MAX=50
REDIS_CLUSTER=true
KAFKA_ENABLED=true
STRIPE_WEBHOOK_TOLERANCE=300
RATE_LIMIT_REQUESTS=1000
RATE_LIMIT_WINDOW=900000
```

### 3. Security Hardening

```javascript
// Security middleware
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS.split(','),
  credentials: true
}));
app.use(rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100
}));
```

### 4. Monitoring Setup

```javascript
// Prometheus metrics
const prometheus = require('prom-client');
const register = new prometheus.Registry();

// Custom metrics
const billingCounter = new prometheus.Counter({
  name: 'billing_transactions_total',
  help: 'Total billing transactions',
  labelNames: ['type', 'status']
});

register.registerMetric(billingCounter);
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues

```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Test connection
psql -h localhost -U billing_user -d billing_db

# Check connection pool
npm run db:check-pool
```

#### 2. Redis Connection Issues

```bash
# Test Redis connection
redis-cli ping

# Check Redis memory
redis-cli info memory

# Clear Redis cache
redis-cli FLUSHDB
```

#### 3. Payment Gateway Issues

```bash
# Test Stripe connectivity
curl https://api.stripe.com/v1/charges \
  -u sk_test_your_key:

# Check webhook signatures
npm run test:webhooks

# Verify API keys
npm run verify:payment-config
```

#### 4. Performance Issues

```bash
# Check Node.js memory
node --inspect src/index.js

# Profile application
npm run profile

# Check database queries
npm run db:analyze-queries
```

### Debug Mode

```bash
# Enable debug logging
DEBUG=billing:* npm start

# Specific module debugging
DEBUG=billing:payment,billing:subscription npm start

# Full debug with stack traces
NODE_ENV=development DEBUG=* npm start
```

### Health Checks

```bash
# Comprehensive health check
curl http://localhost:3401/health/detailed

# Check specific components
curl http://localhost:3401/health/database
curl http://localhost:3401/health/redis
curl http://localhost:3401/health/payment-gateways
```

## Next Steps

1. **Configure monitoring**: Set up Prometheus and Grafana dashboards
2. **Set up CI/CD**: Configure automated testing and deployment
3. **Security audit**: Run security scanning tools
4. **Performance tuning**: Optimize database queries and caching
5. **Documentation**: Generate API documentation with OpenAPI

## Support

- **Documentation**: `/docs` directory
- **API Reference**: `http://localhost:3401/api-docs`
- **Health Check**: `http://localhost:3401/health`
- **Metrics**: `http://localhost:3401/metrics`
- **Support Email**: billing-support@exalt.com

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*