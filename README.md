# Shared Infrastructure Services
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FExalt-Application-Limited%2Fexalt-shared-infrastructure.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FExalt-Application-Limited%2Fexalt-shared-infrastructure?ref=badge_shield)


This repository contains the core infrastructure services that support the entire Clean Social E-commerce Ecosystem.

## Services (23 total)

### Core Infrastructure
- **auth-service** - Authentication and authorization
- **api-gateway** - API gateway and routing
- **config-server** - Configuration management
- **service-registry** - Service discovery (Eureka)

### Communication & Messaging
- **notification-service** - Multi-channel notifications
- **message-broker** - Event messaging
- **translation-service** - Internationalization

### Data & Storage
- **file-storage-service** - File management
- **caching-service** - Redis caching layer
- **logging-service** - Centralized logging

### Security & Compliance
- **kyc-service** - Know Your Customer verification
- **document-verification** - Document validation
- **user-profile-service** - User management

### Monitoring & Operations
- **monitoring-service** - System monitoring
- **tracing-config** - Distributed tracing
- **analytics-engine** - Data analytics

### Business Support
- **payment-processing-service** - Payment handling
- **billing-engine** - Billing operations
- **currency-exchange-service** - Multi-currency support
- **geo-location-service** - Location services

### Development Tools
- **feature-flag-service** - Feature toggles
- **admin-frameworks** - Administrative tools
- **ui-design-system** - Component library

## Technology Stack
- **Backend**: Java 17, Spring Boot 3.x, Maven
- **Frontend**: Node.js, React
- **Database**: PostgreSQL, Redis
- **Message Queue**: Apache Kafka, RabbitMQ
- **Monitoring**: Prometheus, Grafana

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose

### Build All Services
```bash
./build-all-services.sh
```

### Start All Services
```bash
./start-all-services.sh
```

## Repository Structure
```
shared-infrastructure/
├── auth-service/
├── api-gateway/
├── config-server/
├── service-registry/
└── [19 more services...]
```

## Contributing
1. Create feature branch from `main`
2. Make changes with tests
3. Run pre-commit hooks
4. Submit pull request

## Contact
- **Team**: Platform Infrastructure Team
- **Repository**: https://github.com/Exalt-Application-Limited/shared-infrastructure

## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FExalt-Application-Limited%2Fexalt-shared-infrastructure.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FExalt-Application-Limited%2Fexalt-shared-infrastructure?ref=badge_large)