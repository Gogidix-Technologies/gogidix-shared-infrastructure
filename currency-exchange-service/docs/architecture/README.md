# Architecture Documentation - Currency Exchange Service

## Overview

The Currency Exchange Service provides real-time currency conversion capabilities for the Social E-commerce Ecosystem. It handles multi-currency transactions, exchange rate management, and currency conversion calculations for global operations across Europe and Africa.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Overview](#component-overview)
3. [Data Flow](#data-flow)
4. [Technology Stack](#technology-stack)
5. [Architectural Patterns](#architectural-patterns)
6. [Security Architecture](#security-architecture)
7. [Scalability Design](#scalability-design)
8. [Integration Points](#integration-points)

## System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                Currency Exchange Service                     │
├─────────────────────────┬───────────────────────────────────┤
│   Exchange Rate API     │      Currency Conversion          │
├─────────────────────────┼───────────────────────────────────┤
│   Rate Cache Manager    │      Historical Data Store        │
├─────────────────────────┴───────────────────────────────────┤
│                    Core Service Layer                        │
├─────────────────────────────────────────────────────────────┤
│                  Security & Authentication                   │
├─────────────────────────────────────────────────────────────┤
│                     Data Access Layer                        │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Principles

1. **Real-time Processing**: Live exchange rate updates
2. **High Availability**: 99.9% uptime for currency services
3. **Multi-Currency Support**: Global currency coverage
4. **Caching Strategy**: Optimized performance for frequent conversions
5. **Event-Driven**: Rate change notifications

## Component Overview

### Core Components

#### Exchange Rate Management
- **com.exalt.shared.currency.RateProvider**: External rate provider integration
- **com.exalt.shared.currency.RateCache**: Redis-based rate caching
- **com.exalt.shared.currency.RateScheduler**: Automatic rate updates
- **com.exalt.shared.currency.RateValidator**: Rate validation and sanity checks

#### Currency Conversion
- **com.exalt.shared.currency.ConversionService**: Core conversion logic
- **com.exalt.shared.currency.ConversionController**: REST API endpoints
- **com.exalt.shared.currency.ConversionCalculator**: Mathematical operations
- **com.exalt.shared.currency.ConversionHistory**: Transaction history

#### Regional Support
- **com.exalt.shared.currency.RegionalRates**: Region-specific rates
- **com.exalt.shared.currency.CurrencyLocalization**: Locale-aware formatting
- **com.exalt.shared.currency.RegionalPolicyManager**: Regional currency policies

### Supporting Components

| Component | Purpose | Technology |
|-----------|---------|------------|
| RateProvider | External API integration | Node.js/Express |
| CacheManager | Rate caching | Redis |
| ScheduleManager | Rate update scheduling | Node-cron |
| EventEmitter | Rate change notifications | EventEmitter |

### Infrastructure Components

- **Database**: MongoDB for historical rates
- **Cache**: Redis for current rates
- **Message Broker**: Kafka for rate change events
- **External APIs**: Multiple currency providers (Open Exchange, Fixer.io)

## Data Flow

### Currency Conversion Flow

```
Client Request -> Rate Validation -> Cache Check -> Conversion -> Response
                                        ↓
                                   External API (if cache miss)
                                        ↓
                                   Cache Update -> Event Emission
```

### Rate Update Flow

```
Scheduler -> External API -> Rate Validation -> Cache Update -> Event Broadcast
                                                    ↓
                                               Historical Storage
```

## Technology Stack

### Backend Technologies

- **Language**: Node.js (ES6+)
- **Framework**: Express.js
- **Package Manager**: npm
- **Container**: Docker
- **Database**: MongoDB (historical data)

### Framework Technologies

| Type | Technology | Use Case |
|------|------------|----------|
| Web Framework | Express.js | REST APIs |
| Validation | Joi | Input validation |
| Caching | Redis | Rate caching |
| Scheduling | node-cron | Rate updates |
| HTTP Client | Axios | External API calls |

### Development Tools

- **IDE**: Visual Studio Code recommended
- **Version Control**: Git
- **API Documentation**: OpenAPI 3.0
- **Testing**: Jest, Supertest

## Architectural Patterns

### Design Patterns

1. **Strategy Pattern**
   - Multiple currency providers
   - Different conversion algorithms
   - Regional rate policies

2. **Observer Pattern**
   - Rate change notifications
   - Event-driven updates
   - Client subscriptions

3. **Cache-Aside Pattern**
   - Rate caching strategy
   - Cache invalidation
   - Cache warming

4. **Circuit Breaker Pattern**
   - External API protection
   - Fallback mechanisms
   - Service resilience

5. **Adapter Pattern**
   - Multiple provider integration
   - Unified rate interface
   - Provider abstraction

### Communication Patterns

- **REST APIs**: External communication
- **WebSocket**: Real-time rate updates
- **Event Streaming**: Rate change broadcasting via Kafka
- **HTTP Polling**: External provider rate fetching

## Security Architecture

### Authentication & Authorization

- **Method**: JWT-based authentication
- **API Keys**: External provider authentication
- **Rate Limiting**: API usage protection

### Security Layers

1. **API Security**
   - JWT token validation
   - API rate limiting
   - CORS configuration
   - Input sanitization

2. **Data Security**
   - Rate data validation
   - Secure provider connections
   - Audit logging

3. **Provider Security**
   - API key management
   - Secure HTTPS connections
   - Provider failover

### Security Features

- Multiple provider redundancy
- Rate manipulation detection
- Audit trail for all conversions
- Secure configuration management

## Scalability Design

### Horizontal Scaling

- **Service Instances**: Multiple instances behind load balancer
- **Cache**: Redis cluster for distributed caching
- **Database**: MongoDB replica set for read scaling

### Performance Optimization

1. **Caching Strategy**
   - Multi-level caching
   - Rate TTL management
   - Cache warming strategies

2. **API Optimization**
   - Response compression
   - Batch conversion support
   - Efficient rate lookups

3. **Database Optimization**
   - Indexed queries on currency pairs
   - Historical data partitioning
   - Aggregated rate calculations

## Integration Points

### Internal Service Integration

| Service | Integration Method | Purpose |
|---------|-------------------|---------|
| Payment Service | REST API | Transaction currency conversion |
| Analytics Engine | REST/Kafka | Currency analytics |
| Notification Service | Kafka | Rate alert notifications |
| Config Server | REST API | Configuration management |

### External Provider Integration

1. **Primary Providers**
   - Open Exchange Rates API
   - Fixer.io API
   - CurrencyLayer API

2. **Backup Providers**
   - European Central Bank
   - Bank of England
   - Federal Reserve Economic Data

### Integration Patterns

1. **Provider Fallback**
   - Primary provider failure handling
   - Automatic provider switching
   - Rate freshness validation

2. **Event Integration**
   - Rate change broadcasting
   - Conversion event logging
   - Alert notifications

## Deployment Architecture

### Container Strategy

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 3402
CMD ["npm", "start"]
```

### Kubernetes Deployment

- Deployment with 3 replicas minimum
- Horizontal Pod Autoscaler
- ConfigMaps for provider configurations
- Secrets for API keys

## Monitoring and Observability

### Metrics Collection

- Response time monitoring
- Conversion volume tracking
- Provider performance metrics
- Cache hit rate monitoring

### Health Checks

- Liveness probe: `/health/live`
- Readiness probe: `/health/ready`
- Provider connectivity checks

## Disaster Recovery

### Backup Strategy

- Redis data snapshots: Every 6 hours
- MongoDB backups: Daily automated
- Configuration backups: Git versioned

### Recovery Procedures

1. **RTO**: 5 minutes
2. **RPO**: 15 minutes
3. **Automated failover**: Multi-provider redundancy

## Architecture Decision Records (ADRs)

### ADR-001: Node.js Technology Choice

- **Status**: Accepted
- **Context**: Need for high-performance real-time service
- **Decision**: Use Node.js for non-blocking I/O
- **Consequences**: Better concurrency, JavaScript ecosystem

### ADR-002: Multi-Provider Strategy

- **Status**: Accepted
- **Context**: Single provider reliability risk
- **Decision**: Implement multiple provider support
- **Consequences**: Higher reliability, fallback capabilities

### ADR-003: Redis Caching

- **Status**: Accepted
- **Context**: High-frequency rate lookups
- **Decision**: Use Redis for rate caching
- **Consequences**: Improved performance, reduced API calls

## Future Considerations

1. **Blockchain Integration**: Cryptocurrency support
2. **AI Rate Prediction**: Machine learning for rate forecasting
3. **Real-time Streaming**: WebSocket-based live rates
4. **Advanced Analytics**: Rate trend analysis

## References

- [Open Exchange Rates Documentation](https://docs.openexchangerates.org/)
- [Fixer.io API Reference](https://fixer.io/documentation)
- [Node.js Best Practices](https://github.com/goldbergyoni/nodebestpractices)
- [Currency Exchange Best Practices](https://www.investopedia.com/articles/forex/11/why-trade-forex.asp)