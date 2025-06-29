# Shared Infrastructure Domain Documentation

## Overview

The Shared Infrastructure domain provides core platform services that enable the Social E-commerce Ecosystem. These services handle cross-cutting concerns such as authentication, API routing, monitoring, configuration management, and other foundational capabilities that all other domains depend on.

## Domain Architecture

### Service Categories

#### Core Infrastructure Services
- **API Gateway** (Port 8401) - Central entry point for all API requests
- **Service Registry** (Port 8411) - Service discovery and registration
- **Config Server** (Port 8404) - Centralized configuration management
- **Message Broker** (Port 8410) - Asynchronous messaging infrastructure

#### Security & Identity Services
- **Auth Service** (Port 8402) - Authentication and authorization
- **KYC Service** (Port 8408) - Know Your Customer verification
- **Document Verification** (Port 8405) - Document validation and verification

#### Data & Analytics Services
- **Analytics Engine** (Port 3400) - Data analytics and business intelligence
- **Caching Service** (Port 8403) - Distributed caching layer
- **Logging Service** (Port 8409) - Centralized logging infrastructure

#### Business Support Services
- **Billing Engine** (Port 3401) - Billing and invoicing capabilities
- **Currency Exchange Service** (Port 3402) - Multi-currency support
- **Payment Processing Service** - Payment gateway integration
- **Translation Service** - Multi-language support

#### Platform Services
- **Admin Frameworks** (Port 8400) - Administrative tools and dashboards
- **Feature Flag Service** (Port 3403) - Feature toggle management
- **File Storage Service** (Port 8406) - File upload and storage
- **Geo-Location Service** (Port 8407) - Location-based services
- **Notification Service** - Multi-channel notifications
- **User Profile Service** - User profile management

#### Observability Services
- **Monitoring Service** - System monitoring and alerting
- **Tracing Config** - Distributed tracing configuration

## Service Documentation Index

### Java Services (Spring Boot)

1. **[Admin Frameworks](./admin-frameworks/README.md)** - Port 8400
   - [Architecture](./admin-frameworks/docs/architecture/README.md)
   - [Setup Guide](./admin-frameworks/docs/setup/README.md)
   - [Operations](./admin-frameworks/docs/operations/README.md)
   - [API Specification](./admin-frameworks/api-docs/openapi.yaml)

2. **[API Gateway](./api-gateway/README.md)** - Port 8401
   - [Architecture](./api-gateway/docs/architecture/README.md)
   - [Setup Guide](./api-gateway/docs/setup/README.md)
   - [Operations](./api-gateway/docs/operations/README.md)
   - [API Specification](./api-gateway/api-docs/openapi.yaml)

3. **[Auth Service](./auth-service/README.md)** - Port 8402
   - [Architecture](./auth-service/docs/architecture/README.md)
   - [Setup Guide](./auth-service/docs/setup/README.md)
   - [Operations](./auth-service/docs/operations/README.md)
   - [API Specification](./auth-service/api-docs/openapi.yaml)

4. **[Caching Service](./caching-service/README.md)** - Port 8403
   - [Architecture](./caching-service/docs/architecture/README.md)
   - [Setup Guide](./caching-service/docs/setup/README.md)
   - [Operations](./caching-service/docs/operations/README.md)
   - [API Specification](./caching-service/api-docs/openapi.yaml)

5. **[Config Server](./config-server/README.md)** - Port 8404
   - [Architecture](./config-server/docs/architecture/README.md)
   - [Setup Guide](./config-server/docs/setup/README.md)
   - [Operations](./config-server/docs/operations/README.md)
   - [API Specification](./config-server/api-docs/openapi.yaml)

6. **[Document Verification](./document-verification/README.md)** - Port 8405
   - [Architecture](./document-verification/docs/architecture/README.md)
   - [Setup Guide](./document-verification/docs/setup/README.md)
   - [Operations](./document-verification/docs/operations/README.md)
   - [API Specification](./document-verification/api-docs/openapi.yaml)

7. **[File Storage Service](./file-storage-service/README.md)** - Port 8406
   - [Architecture](./file-storage-service/docs/architecture/README.md)
   - [Setup Guide](./file-storage-service/docs/setup/README.md)
   - [Operations](./file-storage-service/docs/operations/README.md)
   - [API Specification](./file-storage-service/api-docs/openapi.yaml)

8. **[Geo-Location Service](./geo-location-service/README.md)** - Port 8407
   - [Architecture](./geo-location-service/docs/architecture/README.md)
   - [Setup Guide](./geo-location-service/docs/setup/README.md)
   - [Operations](./geo-location-service/docs/operations/README.md)
   - [API Specification](./geo-location-service/api-docs/openapi.yaml)

9. **[KYC Service](./kyc-service/README.md)** - Port 8408
   - [Architecture](./kyc-service/docs/architecture/README.md)
   - [Setup Guide](./kyc-service/docs/setup/README.md)
   - [Operations](./kyc-service/docs/operations/README.md)
   - [API Specification](./kyc-service/api-docs/openapi.yaml)

10. **[Logging Service](./logging-service/README.md)** - Port 8409
    - [Architecture](./logging-service/docs/architecture/README.md)
    - [Setup Guide](./logging-service/docs/setup/README.md)
    - [Operations](./logging-service/docs/operations/README.md)
    - [API Specification](./logging-service/api-docs/openapi.yaml)

11. **[Message Broker](./message-broker/README.md)** - Port 8410
    - [Architecture](./message-broker/docs/architecture/README.md)
    - [Setup Guide](./message-broker/docs/setup/README.md)
    - [Operations](./message-broker/docs/operations/README.md)
    - [API Specification](./message-broker/api-docs/openapi.yaml)

12. **[Service Registry](./service-registry/README.md)** - Port 8411
    - [Architecture](./service-registry/docs/architecture/README.md)
    - [Setup Guide](./service-registry/docs/setup/README.md)
    - [Operations](./service-registry/docs/operations/README.md)
    - [API Specification](./service-registry/api-docs/openapi.yaml)

13. **[Monitoring Service](./monitoring-service/README.md)** - Java Service
    - [Architecture](./monitoring-service/docs/architecture/README.md)
    - [Setup Guide](./monitoring-service/docs/setup/README.md)
    - [Operations](./monitoring-service/docs/operations/README.md)
    - [API Specification](./monitoring-service/api-docs/openapi.yaml)

14. **[Notification Service](./notification-service/README.md)** - Java Service
    - [Architecture](./notification-service/docs/architecture/README.md)
    - [Setup Guide](./notification-service/docs/setup/README.md)
    - [Operations](./notification-service/docs/operations/README.md)
    - [API Specification](./notification-service/api-docs/openapi.yaml)

15. **[Payment Processing Service](./payment-processing-service/README.md)** - Java Service
    - [Architecture](./payment-processing-service/docs/architecture/README.md)
    - [Setup Guide](./payment-processing-service/docs/setup/README.md)
    - [Operations](./payment-processing-service/docs/operations/README.md)
    - [API Specification](./payment-processing-service/api-docs/openapi.yaml)

16. **[Tracing Config](./tracing-config/README.md)** - Java Service
    - [Architecture](./tracing-config/docs/architecture/README.md)
    - [Setup Guide](./tracing-config/docs/setup/README.md)
    - [Operations](./tracing-config/docs/operations/README.md)
    - [API Specification](./tracing-config/api-docs/openapi.yaml)

17. **[Translation Service](./translation-service/README.md)** - Java Service
    - [Architecture](./translation-service/docs/architecture/README.md)
    - [Setup Guide](./translation-service/docs/setup/README.md)
    - [Operations](./translation-service/docs/operations/README.md)
    - [API Specification](./translation-service/api-docs/openapi.yaml)

18. **[User Profile Service](./user-profile-service/README.md)** - Java Service
    - [Architecture](./user-profile-service/docs/architecture/README.md)
    - [Setup Guide](./user-profile-service/docs/setup/README.md)
    - [Operations](./user-profile-service/docs/operations/README.md)
    - [API Specification](./user-profile-service/api-docs/openapi.yaml)

### Node.js Services

1. **[Analytics Engine](./analytics-engine/README.md)** - Port 3400
   - [Architecture](./analytics-engine/docs/architecture/README.md)
   - [Setup Guide](./analytics-engine/docs/setup/README.md)
   - [Operations](./analytics-engine/docs/operations/README.md)
   - [API Specification](./analytics-engine/api-docs/openapi.yaml)

2. **[Billing Engine](./billing-engine/README.md)** - Port 3401
   - [Architecture](./billing-engine/docs/architecture/README.md)
   - [Setup Guide](./billing-engine/docs/setup/README.md)
   - [Operations](./billing-engine/docs/operations/README.md)
   - [API Specification](./billing-engine/api-docs/openapi.yaml)

3. **[Currency Exchange Service](./currency-exchange-service/README.md)** - Port 3402
   - [Architecture](./currency-exchange-service/docs/architecture/README.md)
   - [Setup Guide](./currency-exchange-service/docs/setup/README.md)
   - [Operations](./currency-exchange-service/docs/operations/README.md)
   - [API Specification](./currency-exchange-service/api-docs/openapi.yaml)

4. **[Feature Flag Service](./feature-flag-service/README.md)** - Port 3403
   - [Architecture](./feature-flag-service/docs/architecture/README.md)
   - [Setup Guide](./feature-flag-service/docs/setup/README.md)
   - [Operations](./feature-flag-service/docs/operations/README.md)
   - [API Specification](./feature-flag-service/api-docs/openapi.yaml)

## Getting Started

### Prerequisites

- Java 17+
- Node.js 16+
- Docker & Docker Compose
- Maven 3.8+
- PostgreSQL 14+
- Redis 6.2+
- Apache Kafka 3.0+

### Quick Start

1. Clone the repository
2. Set up environment variables:
   ```bash
   cp .env.template .env
   # Edit .env with your configuration
   ```

3. Start infrastructure services:
   ```bash
   docker-compose -f docker-compose.infrastructure.yml up -d
   ```

4. Start core services in order:
   ```bash
   # 1. Service Registry
   cd service-registry && mvn spring-boot:run
   
   # 2. Config Server
   cd config-server && mvn spring-boot:run
   
   # 3. API Gateway
   cd api-gateway && mvn spring-boot:run
   
   # 4. Other services as needed
   ```

## Architecture Principles

1. **Microservices Architecture**: Each service is independently deployable
2. **Domain-Driven Design**: Services organized by business capabilities
3. **API-First**: Well-defined contracts between services
4. **Cloud-Native**: Containerized and orchestration-ready
5. **Security by Design**: Authentication and authorization at every layer

## Inter-Service Communication

### Synchronous Communication
- REST APIs via API Gateway
- Service-to-service calls via Service Registry

### Asynchronous Communication
- Event-driven architecture using Kafka
- Message patterns: Commands, Events, Queries

## Security Guidelines

- All external APIs secured with JWT authentication
- Service-to-service communication uses mutual TLS
- Sensitive data encrypted at rest and in transit
- Regular security audits and vulnerability scanning

## Monitoring and Observability

- **Metrics**: Prometheus + Grafana dashboards
- **Logging**: Centralized via ELK stack
- **Tracing**: Distributed tracing with Jaeger
- **Alerting**: Configured for critical metrics

## Support and Contact

- **Documentation**: This repository
- **Issues**: GitHub Issues
- **Team Chat**: Slack #shared-infrastructure
- **Email**: infrastructure-team@exalt.com

## License

Copyright (c) 2024 Exalt Application Limited. All rights reserved.