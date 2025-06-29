# Setup Guide - Currency Exchange Service

## Overview

This document provides comprehensive setup instructions for the Currency Exchange Service, including local development environment, Docker setup, and cloud deployment.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Setup](#kubernetes-setup)
5. [Configuration](#configuration)
6. [Verification](#verification)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: Minimum 5GB free space
- **Network**: Stable internet connection for external API access

### Required Software

#### Development Tools

```bash
# Node.js Runtime
Node.js 18+ (LTS recommended)

# Package Manager
npm 8+ or yarn 1.22+

# IDE (Recommended)
Visual Studio Code with Node.js extensions

# Version Control
Git 2.30+
```

#### Container and Orchestration

```bash
# Docker
Docker Desktop 4.0+ or Docker Engine 20.10+
Docker Compose 2.0+

# Kubernetes (for cloud deployment)
kubectl 1.24+
helm 3.8+
```

#### Database and Caching

```bash
# Database
MongoDB 6.0+

# Cache
Redis 6.2+

# Message Broker
Apache Kafka 3.0+
```

### Installation Instructions

#### Node.js 18

**Windows:**
```powershell
# Using Chocolatey
choco install nodejs

# Or download from official website
# https://nodejs.org/en/download/
```

**macOS:**
```bash
# Using Homebrew
brew install node@18

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/node@18/bin:$PATH"' >> ~/.zshrc
```

**Linux:**
```bash
# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# CentOS/RHEL
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/currency-exchange-service
```

### 2. Environment Configuration

```bash
# Copy environment template
cp .env.template .env

# Edit with your local settings
nano .env
```

Required environment variables:
```properties
# Service Configuration
PORT=3402
NODE_ENV=development

# Database Configuration
MONGODB_URI=mongodb://localhost:27017/currency_exchange_db
MONGODB_USER=currency_user
MONGODB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# External Currency Providers
OPEN_EXCHANGE_RATES_API_KEY=your-api-key
FIXER_IO_API_KEY=your-api-key
CURRENCY_LAYER_API_KEY=your-api-key

# Security
JWT_SECRET=your-secret-key
API_RATE_LIMIT=1000

# Logging
LOG_LEVEL=debug
LOG_FILE=/var/log/currency-exchange/service.log
```

### 3. Infrastructure Setup

#### Option A: Docker Compose (Recommended)

```bash
# Start infrastructure services
docker-compose -f docker-compose.yml up -d mongodb redis kafka

# Verify services are running
docker-compose ps
```

#### Option B: Native Installation

**MongoDB:**
```bash
# Create database and user
mongo
> use currency_exchange_db
> db.createUser({
    user: "currency_user",
    pwd: "secure_password",
    roles: ["readWrite"]
  })
```

**Redis:**
```bash
# Start Redis server
redis-server

# Test connection
redis-cli ping
```

### 4. Install Dependencies

```bash
# Install Node.js dependencies
npm install

# Or using yarn
yarn install

# Install development tools
npm install -g nodemon eslint
```

### 5. Database Initialization

```bash
# Run database migrations
npm run migrate

# Seed initial currency data
npm run seed:currencies

# Initialize exchange rate cache
npm run cache:warm
```

### 6. Start the Service

```bash
# Development mode with hot reload
npm run dev

# Or using nodemon directly
nodemon src/index.js

# Production mode
npm start
```

### 7. Initialize Currency Data

```bash
# Load supported currencies
curl -X POST http://localhost:3402/api/v1/currencies/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN"

# Fetch initial exchange rates
curl -X POST http://localhost:3402/api/v1/rates/update \
  -H "Authorization: Bearer $TOKEN"
```

## Docker Setup

### 1. Build Docker Image

```bash
# Build the image
docker build -t currency-exchange-service:latest .

# Or use the build script
./scripts/build-docker.sh
```

### 2. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f currency-exchange-service

# Scale the service
docker-compose up -d --scale currency-exchange-service=3
```

### 3. Docker Configuration

```yaml
# docker-compose.yml
version: '3.8'
services:
  currency-exchange-service:
    image: currency-exchange-service:latest
    ports:
      - "3402:3402"
    environment:
      - NODE_ENV=docker
      - MONGODB_URI=mongodb://mongodb:27017/currency_exchange_db
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - mongodb
      - redis
      - kafka
    restart: unless-stopped
```

## Kubernetes Setup

### 1. Prerequisites

```bash
# Create namespace
kubectl create namespace shared-infrastructure

# Create secrets
kubectl create secret generic currency-exchange-secrets \
  --from-literal=mongodb-password=secure_password \
  --from-literal=jwt-secret=your-secret-key \
  --from-literal=open-exchange-api-key=your-api-key \
  -n shared-infrastructure
```

### 2. Deploy with Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/ -n shared-infrastructure

# Check deployment status
kubectl get pods -l app=currency-exchange-service -n shared-infrastructure

# View logs
kubectl logs -f deployment/currency-exchange-service -n shared-infrastructure
```

### 3. Helm Deployment

```bash
# Add Helm repository
helm repo add exalt https://charts.exalt.com
helm repo update

# Install the chart
helm install currency-exchange-service exalt/currency-exchange-service \
  --namespace shared-infrastructure \
  --set image.tag=latest \
  --set service.port=3402
```

## Configuration

### Application Configuration

```javascript
// config/default.js
module.exports = {
  server: {
    port: process.env.PORT || 3402,
    host: process.env.HOST || '0.0.0.0'
  },
  database: {
    mongodb: {
      uri: process.env.MONGODB_URI || 'mongodb://localhost:27017/currency_exchange_db',
      options: {
        useNewUrlParser: true,
        useUnifiedTopology: true,
        maxPoolSize: 10,
        serverSelectionTimeoutMS: 5000
      }
    }
  },
  cache: {
    redis: {
      host: process.env.REDIS_HOST || 'localhost',
      port: process.env.REDIS_PORT || 6379,
      ttl: 300 // 5 minutes
    }
  },
  providers: {
    openExchangeRates: {
      apiKey: process.env.OPEN_EXCHANGE_RATES_API_KEY,
      baseUrl: 'https://openexchangerates.org/api/',
      timeout: 5000
    },
    fixerIo: {
      apiKey: process.env.FIXER_IO_API_KEY,
      baseUrl: 'http://data.fixer.io/api/',
      timeout: 5000
    }
  },
  scheduling: {
    rateUpdate: '*/15 * * * *', // Every 15 minutes
    historicalData: '0 2 * * *'  // Daily at 2 AM
  }
};
```

### Currency Configuration

```javascript
// config/currencies.js
module.exports = {
  supportedCurrencies: [
    'USD', 'EUR', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY',
    'SEK', 'NZD', 'NOK', 'DKK', 'PLN', 'CZK', 'HUF', 'BGN',
    'RON', 'HRK', 'RUB', 'TRY', 'BRL', 'INR', 'KRW', 'SGD',
    'THB', 'MYR', 'IDR', 'PHP', 'VND', 'ZAR', 'EGP', 'MAD',
    'TND', 'GHS', 'NGN', 'KES', 'UGX', 'TZS', 'RWF', 'ETB'
  ],
  baseCurrency: 'USD',
  regionalPairs: {
    europe: ['EUR', 'GBP', 'CHF', 'SEK', 'NOK', 'DKK', 'PLN'],
    africa: ['ZAR', 'EGP', 'MAD', 'TND', 'GHS', 'NGN', 'KES']
  },
  precision: 4,
  rounding: 'ROUND_HALF_UP'
};
```

## Verification

### 1. Health Checks

```bash
# Check service health
curl http://localhost:3402/health

# Check currency provider status
curl http://localhost:3402/api/v1/providers/status

# Verify database connection
curl http://localhost:3402/health/database
```

### 2. Currency Testing

```bash
# Get available currencies
curl http://localhost:3402/api/v1/currencies

# Get current exchange rates
curl http://localhost:3402/api/v1/rates/USD

# Test currency conversion
curl -X POST http://localhost:3402/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{
    "from": "USD",
    "to": "EUR",
    "amount": 100
  }'
```

### 3. Integration Testing

```bash
# Run unit tests
npm test

# Run integration tests
npm run test:integration

# Run specific test suites
npm test -- --grep "conversion"
npm test -- --grep "rate-provider"
```

## Troubleshooting

### Common Issues

#### 1. Port Conflicts

**Problem**: Service fails to start due to port 3402 already in use

**Solution**:
```bash
# Find process using port
lsof -i :3402

# Change port in configuration
export PORT=3403
```

#### 2. MongoDB Connection Issues

**Problem**: Cannot connect to MongoDB

**Solutions**:
```bash
# Check MongoDB is running
docker ps | grep mongodb

# Test connection
mongo $MONGODB_URI

# Check credentials
echo $MONGODB_USER
```

#### 3. External API Rate Limits

**Problem**: Currency provider API rate limit exceeded

**Solutions**:
```bash
# Check provider status
curl http://localhost:3402/api/v1/providers/status

# Switch to backup provider
curl -X POST http://localhost:3402/api/v1/providers/switch \
  -H "Content-Type: application/json" \
  -d '{"provider": "fixer-io"}'

# Check rate limit status
curl http://localhost:3402/api/v1/providers/limits
```

#### 4. Cache Issues

**Problem**: Stale or missing exchange rates

**Solutions**:
```bash
# Clear rate cache
redis-cli FLUSHDB

# Force rate update
curl -X POST http://localhost:3402/api/v1/rates/refresh

# Check cache status
curl http://localhost:3402/api/v1/cache/status
```

### Debug Mode

```bash
# Enable debug logging
export LOG_LEVEL=debug

# Or in package.json
DEBUG=currency-exchange:* npm run dev

# View detailed logs
tail -f logs/currency-exchange.log | grep DEBUG
```

### Performance Tuning

```bash
# Node.js memory tuning
export NODE_OPTIONS="--max-old-space-size=2048"

# Redis memory optimization
redis-cli CONFIG SET maxmemory 256mb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

## Next Steps

1. **Configure Providers**: Set up currency provider API keys
2. **Set Up Monitoring**: Configure Prometheus and Grafana
3. **Enable Caching**: Optimize Redis caching strategy
4. **Create Schedules**: Set up automatic rate updates
5. **Deploy to Production**: Follow production deployment guide

## Support

- **Documentation**: `/docs`
- **API Reference**: `http://localhost:3402/docs`
- **Issues**: GitHub Issues
- **Team Chat**: Slack #currency-exchange
- **Email**: currency-team@exalt.com