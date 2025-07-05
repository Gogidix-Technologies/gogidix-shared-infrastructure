# Caching Service

## Overview

Caching Service is a core Java-based microservice in the Shared Infrastructure domain of the Social E-commerce Ecosystem. This service provides essential infrastructure capabilities that support the entire platform.

### Service Details

- **Service Type**: Infrastructure Service
- **Domain**: Shared Infrastructure  
- **Technology**: Java 17 + Spring Boot 3.x
- **Port**: 8403
- **Health Check**: `http://localhost:8403/actuator/health`
- **API Documentation**: `http://localhost:8403/swagger-ui.html`

## Architecture

### Position in Ecosystem

Caching Service serves as a foundational component in the microservices architecture, providing critical infrastructure services that enable other domain services to function effectively.

### Key Responsibilities

- Infrastructure service management
- Cross-cutting concerns handling
- Platform reliability and performance
- Service integration facilitation

### Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka
- **Cache**: Redis
- **Build Tool**: Maven
- **Container**: Docker

## API Endpoints

### Health Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/actuator/health` | Service health status |
| GET    | `/actuator/health/liveness` | Liveness probe |
| GET    | `/actuator/health/readiness` | Readiness probe |

### Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1/status` | Service status |
| GET    | `/api/v1/info` | Service information |

## Dependencies

### Infrastructure Dependencies

- PostgreSQL Database
- Redis Cache
- Kafka Message Broker
- Service Registry (Eureka)
- Config Server

## Configuration

### Environment Variables

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=caching_service_db
DB_USER=caching_service_user
DB_PASSWORD=secure_password

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Service Configuration
SERVER_PORT=8403
SPRING_PROFILES_ACTIVE=dev
```

## Development

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+
- Redis 6.2+

### Local Setup

1. Clone and navigate to service directory:
```bash
git clone <repository-url>
cd shared-infrastructure/caching-service
```

2. Set up environment:
```bash
cp .env.template .env
# Edit .env with your configuration
```

3. Start dependencies:
```bash
docker-compose up -d postgres redis kafka
```

4. Run the service:
```bash
mvn spring-boot:run
```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests  
mvn verify

# With coverage
mvn test jacoco:report
```

## Deployment

### Docker

```bash
# Build image
docker build -t caching-service:latest .

# Run container
docker run -p 8403:8403 \
  --env-file .env \
  caching-service:latest
```

### Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=caching-service
```

## Monitoring

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`

### Metrics

- **Prometheus**: `/actuator/prometheus`
- **JVM Metrics**: Memory, GC, threads
- **Application Metrics**: Custom business metrics

### Logging

- **Log Level**: Configurable via `LOG_LEVEL` environment variable
- **Log Format**: JSON structured logging
- **Correlation**: Request tracing with correlation IDs

## Security

### Authentication

- JWT-based authentication for external APIs
- Service-to-service authentication via mTLS

### Authorization

- Role-based access control (RBAC)
- Fine-grained permissions

## Troubleshooting

### Common Issues

1. **Service startup failures**
   - Check database connectivity
   - Verify environment variables
   - Check port availability

2. **Database connection issues**
   - Verify database is running
   - Check credentials
   - Test network connectivity

### Debug Mode

```bash
export LOG_LEVEL=DEBUG
mvn spring-boot:run
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
