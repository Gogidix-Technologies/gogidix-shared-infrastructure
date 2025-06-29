#!/bin/bash

# Services array - Java services
JAVA_SERVICES=(
    "admin-frameworks:8400"
    "api-gateway:8401" 
    "auth-service:8402"
    "caching-service:8403"
    "config-server:8404"
    "document-verification:8405"
    "file-storage-service:8406"
    "geo-location-service:8407"
    "kyc-service:8408"
    "logging-service:8409"
    "message-broker:8410"
    "service-registry:8411"
    "monitoring-service:TBD"
    "notification-service:TBD"
    "payment-processing-service:TBD"
    "tracing-config:TBD"
    "translation-service:TBD"
    "user-profile-service:TBD"
)

# Node.js services
NODE_SERVICES=(
    "analytics-engine:3400"
    "billing-engine:3401"
    "currency-exchange-service:3402"
    "feature-flag-service:3403"
)

# Function to create enhanced README for Java services
create_java_readme() {
    local service_name=$1
    local port=$2
    local service_title=$(echo $service_name  < /dev/null |  sed 's/-/ /g' | sed 's/\b\w/\U&/g')
    
    cat > "${service_name}/README.md" << README_EOF
# ${service_title}

## Overview

${service_title} is a core Java-based microservice in the Shared Infrastructure domain of the Social E-commerce Ecosystem. This service provides essential infrastructure capabilities that support the entire platform.

### Service Details

- **Service Type**: Infrastructure Service
- **Domain**: Shared Infrastructure  
- **Technology**: Java 17 + Spring Boot 3.x
- **Port**: ${port}
- **Health Check**: \`http://localhost:${port}/actuator/health\`
- **API Documentation**: \`http://localhost:${port}/swagger-ui.html\`

## Architecture

### Position in Ecosystem

${service_title} serves as a foundational component in the microservices architecture, providing critical infrastructure services that enable other domain services to function effectively.

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
| GET    | \`/actuator/health\` | Service health status |
| GET    | \`/actuator/health/liveness\` | Liveness probe |
| GET    | \`/actuator/health/readiness\` | Readiness probe |

### Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | \`/api/v1/status\` | Service status |
| GET    | \`/api/v1/info\` | Service information |

## Dependencies

### Infrastructure Dependencies

- PostgreSQL Database
- Redis Cache
- Kafka Message Broker
- Service Registry (Eureka)
- Config Server

## Configuration

### Environment Variables

\`\`\`bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=${service_name//-/_}_db
DB_USER=${service_name//-/_}_user
DB_PASSWORD=secure_password

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Service Configuration
SERVER_PORT=${port}
SPRING_PROFILES_ACTIVE=dev
\`\`\`

## Development

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 14+
- Redis 6.2+

### Local Setup

1. Clone and navigate to service directory:
\`\`\`bash
git clone <repository-url>
cd shared-infrastructure/${service_name}
\`\`\`

2. Set up environment:
\`\`\`bash
cp .env.template .env
# Edit .env with your configuration
\`\`\`

3. Start dependencies:
\`\`\`bash
docker-compose up -d postgres redis kafka
\`\`\`

4. Run the service:
\`\`\`bash
mvn spring-boot:run
\`\`\`

### Running Tests

\`\`\`bash
# Unit tests
mvn test

# Integration tests  
mvn verify

# With coverage
mvn test jacoco:report
\`\`\`

## Deployment

### Docker

\`\`\`bash
# Build image
docker build -t ${service_name}:latest .

# Run container
docker run -p ${port}:${port} \\
  --env-file .env \\
  ${service_name}:latest
\`\`\`

### Kubernetes

\`\`\`bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=${service_name}
\`\`\`

## Monitoring

### Health Checks

- **Liveness**: \`/actuator/health/liveness\`
- **Readiness**: \`/actuator/health/readiness\`

### Metrics

- **Prometheus**: \`/actuator/prometheus\`
- **JVM Metrics**: Memory, GC, threads
- **Application Metrics**: Custom business metrics

### Logging

- **Log Level**: Configurable via \`LOG_LEVEL\` environment variable
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

\`\`\`bash
export LOG_LEVEL=DEBUG
mvn spring-boot:run
\`\`\`

## Related Documentation

- [Architecture Documentation](./docs/architecture/README.md)
- [Setup Guide](./docs/setup/README.md)
- [Operations Guide](./docs/operations/README.md)
- [API Specification](./api-docs/openapi.yaml)

## Contact

- **Team**: Shared Infrastructure Team
- **Slack Channel**: #shared-infrastructure
- **Email**: infrastructure-team@exalt.com

## License

Copyright (c) 2024 Exalt Application Limited. All rights reserved.
README_EOF
}

# Function to create enhanced README for Node.js services  
create_node_readme() {
    local service_name=$1
    local port=$2
    local service_title=$(echo $service_name | sed 's/-/ /g' | sed 's/\b\w/\U&/g')
    
    cat > "${service_name}/README.md" << README_EOF
# ${service_title}

## Overview

${service_title} is a Node.js-based microservice in the Shared Infrastructure domain of the Social E-commerce Ecosystem. This service provides specialized functionality using modern JavaScript technologies.

### Service Details

- **Service Type**: Infrastructure Service
- **Domain**: Shared Infrastructure
- **Technology**: Node.js + Express.js
- **Port**: ${port}
- **Health Check**: \`http://localhost:${port}/health\`
- **API Documentation**: \`http://localhost:${port}/api-docs\`

## Architecture

### Position in Ecosystem

${service_title} operates as a high-performance microservice leveraging Node.js's event-driven architecture for optimal handling of I/O operations and real-time processing.

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
| GET    | \`/health\` | Service health status |
| GET    | \`/health/live\` | Liveness probe |
| GET    | \`/health/ready\` | Readiness probe |

### Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | \`/api/v1/status\` | Service status |
| GET    | \`/api/v1/info\` | Service information |

## Dependencies

### Infrastructure Dependencies

- MongoDB/PostgreSQL Database
- Redis Cache
- Kafka Message Broker
- Service Registry

## Configuration

### Environment Variables

\`\`\`bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=${service_name//-/_}_db
DB_USER=${service_name//-/_}_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Configuration
PORT=${port}
NODE_ENV=development
\`\`\`

## Development

### Prerequisites

- Node.js 18+
- npm 8+
- Docker & Docker Compose
- PostgreSQL/MongoDB
- Redis 6.2+

### Local Setup

1. Clone and navigate to service directory:
\`\`\`bash
git clone <repository-url>
cd shared-infrastructure/${service_name}
\`\`\`

2. Install dependencies:
\`\`\`bash
npm install
\`\`\`

3. Set up environment:
\`\`\`bash
cp .env.template .env
# Edit .env with your configuration
\`\`\`

4. Start dependencies:
\`\`\`bash
docker-compose up -d postgres redis kafka
\`\`\`

5. Run the service:
\`\`\`bash
npm start
\`\`\`

### Running Tests

\`\`\`bash
# Unit tests
npm test

# Integration tests
npm run test:integration

# Coverage
npm run test:coverage
\`\`\`

## Deployment

### Docker

\`\`\`bash
# Build image
docker build -t ${service_name}:latest .

# Run container
docker run -p ${port}:${port} \\
  --env-file .env \\
  ${service_name}:latest
\`\`\`

### Kubernetes

\`\`\`bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment
kubectl get pods -l app=${service_name}
\`\`\`

## Monitoring

### Health Checks

- **Liveness**: \`/health/live\`
- **Readiness**: \`/health/ready\`

### Metrics

- **Prometheus**: \`/metrics\`
- **Node.js Metrics**: Event loop, memory, CPU
- **Application Metrics**: Custom business metrics

### Logging

- **Log Level**: Configurable via \`LOG_LEVEL\` environment variable
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

\`\`\`bash
export LOG_LEVEL=debug
npm run dev
\`\`\`

## Related Documentation

- [Architecture Documentation](./docs/architecture/README.md)
- [Setup Guide](./docs/setup/README.md)
- [Operations Guide](./docs/operations/README.md)
- [API Specification](./api-docs/openapi.yaml)

## Contact

- **Team**: Shared Infrastructure Team
- **Slack Channel**: #shared-infrastructure
- **Email**: infrastructure-team@exalt.com

## License

Copyright (c) 2024 Exalt Application Limited. All rights reserved.
README_EOF
}

# Create documentation for all services
echo "Creating comprehensive documentation for all services..."

# Process Java services
for service_port in "${JAVA_SERVICES[@]}"; do
    IFS=':' read -r service port <<< "$service_port"
    echo "Creating documentation for Java service: $service"
    
    # Create directories
    mkdir -p "$service/docs/architecture" "$service/docs/setup" "$service/docs/operations"
    
    # Create enhanced README
    create_java_readme "$service" "$port"
    
    # Skip if API docs already exist
    if [[ ! -f "$service/api-docs/openapi.yaml" ]]; then
        mkdir -p "$service/api-docs"
        # Create a basic OpenAPI spec
        cat > "$service/api-docs/openapi.yaml" << 'OPENAPI_EOF'
openapi: 3.0.3
info:
  title: "SERVICE_TITLE API"
  description: "RESTful API for SERVICE_TITLE service"
  version: "1.0.0"
servers:
  - url: "http://localhost:SERVICE_PORT/api/v1"
paths:
  /health:
    get:
      summary: "Health check"
      responses:
        '200':
          description: "Service is healthy"
OPENAPI_EOF
        # Replace placeholders
        sed -i "s/SERVICE_TITLE/$(echo $service | sed 's/-/ /g' | sed 's/\b\w/\U&/g')/g" "$service/api-docs/openapi.yaml"
        sed -i "s/SERVICE_PORT/$port/g" "$service/api-docs/openapi.yaml"
    fi
done

# Process Node.js services
for service_port in "${NODE_SERVICES[@]}"; do
    IFS=':' read -r service port <<< "$service_port"
    echo "Creating documentation for Node.js service: $service"
    
    # Create directories
    mkdir -p "$service/docs/architecture" "$service/docs/setup" "$service/docs/operations"
    
    # Create enhanced README
    create_node_readme "$service" "$port"
    
    # Skip if API docs already exist
    if [[ ! -f "$service/api-docs/openapi.yaml" ]]; then
        mkdir -p "$service/api-docs"
        # Create a basic OpenAPI spec
        cat > "$service/api-docs/openapi.yaml" << 'OPENAPI_EOF'
openapi: 3.0.3
info:
  title: "SERVICE_TITLE API"
  description: "RESTful API for SERVICE_TITLE service"
  version: "1.0.0"
servers:
  - url: "http://localhost:SERVICE_PORT/api/v1"
paths:
  /health:
    get:
      summary: "Health check"
      responses:
        '200':
          description: "Service is healthy"
OPENAPI_EOF
        # Replace placeholders
        sed -i "s/SERVICE_TITLE/$(echo $service | sed 's/-/ /g' | sed 's/\b\w/\U&/g')/g" "$service/api-docs/openapi.yaml"
        sed -i "s/SERVICE_PORT/$port/g" "$service/api-docs/openapi.yaml"
    fi
done

echo "Documentation generation completed!"
