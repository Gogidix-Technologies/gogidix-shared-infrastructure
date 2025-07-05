# Currency Exchange Service

## Overview

Currency Exchange Service is a Node.js-based microservice in the Shared Infrastructure domain of the Social E-commerce Ecosystem. This service provides specialized functionality using modern JavaScript technologies.

### Service Details

- **Service Type**: Infrastructure Service
- **Domain**: Shared Infrastructure
- **Technology**: Node.js + Express.js
- **Port**: 3402
- **Health Check**: `http://localhost:3402/health`
- **API Documentation**: `http://localhost:3402/api-docs`

## Architecture

### Position in Ecosystem

Currency Exchange Service operates as a high-performance microservice leveraging Node.js's event-driven architecture for optimal handling of I/O operations and real-time processing.

### Key Responsibilities

- Specialized data processing
- Real-time event handling
- API service provision
- Integration with external systems

### Technology Stack

- **Language**: Node.js 18+
- **Framework**: Express.js
- **Database**: MongoDB/PostgreSQL
- **Messaging**: Apache Kafka
- **Cache**: Redis
- **Build Tool**: npm
- **Container**: Docker

## API Endpoints

### Health Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/health` | Service health status |
| GET    | `/health/live` | Liveness probe |
| GET    | `/health/ready` | Readiness probe |

### Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1/status` | Service status |
| GET    | `/api/v1/info` | Service information |

## Dependencies

### Infrastructure Dependencies

- MongoDB/PostgreSQL Database
- Redis Cache
- Kafka Message Broker
- Service Registry

## Configuration

### Environment Variables

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=currency_exchange_service_db
DB_USER=currency_exchange_service_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Configuration
PORT=3402
NODE_ENV=development
```

## Development

### Prerequisites

- Node.js 18+
- npm 8+
- Docker & Docker Compose
- PostgreSQL/MongoDB
- Redis 6.2+

### Local Setup

1. Clone and navigate to service directory:
```bash
git clone <repository-url>
cd shared-infrastructure/currency-exchange-service
```

2. Install dependencies:
```bash
npm install
```

3. Set up environment:
```bash
cp .env.template .env
# Edit .env with your configuration
```

4. Start dependencies:
```bash
docker-compose up -d postgres redis kafka
```

5. Run the service:
```bash
npm start
```

### Running Tests

```bash
# Unit tests
npm test

# Integration tests
npm run test:integration

# Coverage
npm run test:coverage
```

## Deployment

### Docker

```bash
# Build image
docker build -t currency-exchange-service:latest .

# Run container
docker run -p 3402:3402 \
  --env-file .env \
  currency-exchange-service:latest
```

### Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=currency-exchange-service
```

## Monitoring

### Health Checks

- **Liveness**: `/health/live`
- **Readiness**: `/health/ready`

### Metrics

- **Prometheus**: `/metrics`
- **Node.js Metrics**: Event loop, memory, CPU
- **Application Metrics**: Custom business metrics

### Logging

- **Log Level**: Configurable via `LOG_LEVEL` environment variable
- **Log Format**: JSON structured logging
- **Correlation**: Request tracing with correlation IDs

## Security

### Authentication

- JWT-based authentication
- API key authentication for service-to-service

### Authorization

- Role-based access control
- Rate limiting and throttling

## Troubleshooting

### Common Issues

1. **Service startup failures**
   - Check Node.js version compatibility
   - Verify environment variables
   - Check port availability

2. **Database connection issues**
   - Verify database is running
   - Check connection strings
   - Test network connectivity

### Debug Mode

```bash
export LOG_LEVEL=debug
npm run dev
```

## Related Documentation

- [Architecture Documentation](./docs/architecture/README.md)
- [Setup Guide](./docs/setup/README.md)
- [Operations Guide](./docs/operations/README.md)
- [API Specification](./api-docs/openapi.yaml)

## Contact

- **Team**: Shared Infrastructure Team
- **Slack Channel**: #shared-infrastructure
- **Email**: infrastructure-team@gogidix.com

## License

Copyright (c) 2024 Gogidix Application Limited. All rights reserved.
