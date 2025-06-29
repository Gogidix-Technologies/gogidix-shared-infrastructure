# Tracing Config - Setup Guide

## Prerequisites

### System Requirements

#### Minimum Requirements
- **CPU**: 2 cores
- **RAM**: 4GB
- **Storage**: 20GB SSD
- **Network**: 1Gbps

#### Recommended Requirements (Production)
- **CPU**: 4+ cores
- **RAM**: 8GB+
- **Storage**: 100GB+ SSD with high IOPS
- **Network**: 10Gbps+

### Software Requirements

#### Development Environment
- **Java**: OpenJDK 17+ (Amazon Corretto recommended)
- **Maven**: 3.8.0+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Git**: 2.30+

#### Production Environment
- **Kubernetes**: 1.24+
- **Helm**: 3.8+
- **PostgreSQL**: 14+
- **Redis**: 6.2+
- **Elasticsearch**: 8.0+ (optional)

### Verification Commands
```bash
# Verify Java version
java -version
# Expected: openjdk version "17.0.x"

# Verify Maven version
mvn -version
# Expected: Apache Maven 3.8.x

# Verify Docker version
docker --version
# Expected: Docker version 20.10.x

# Verify Kubernetes version
kubectl version --client
# Expected: Client Version: v1.24.x
```

## Local Development Setup

### 1. Repository Setup

#### Clone Repository
```bash
# Clone the main repository
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/tracing-config

# Verify directory structure
ls -la
# Expected: Dockerfile, pom.xml, src/, k8s/, docs/
```

#### Environment Configuration
```bash
# Create environment file from template
cp .env.template .env

# Edit environment variables
nano .env
```

#### Environment Variables (.env)
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tracing_config_db
DB_USERNAME=tracing_user
DB_PASSWORD=tracing_secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=12
REDIS_PASSWORD=redis_secure_password

# Elasticsearch Configuration (Optional)
ELASTICSEARCH_HOST=localhost
ELASTICSEARCH_PORT=9200
ELASTICSEARCH_USERNAME=elastic
ELASTICSEARCH_PASSWORD=elastic_password

# Service Configuration
SERVER_PORT=8093
SPRING_PROFILES_ACTIVE=development
LOG_LEVEL=INFO

# Tracing Configuration
JAEGER_ENDPOINT=http://localhost:14268/api/traces
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans

# Security Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-min-256-bits
ENCRYPTION_KEY=your-aes-256-encryption-key

# Service Registry
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Monitoring
PROMETHEUS_ENABLED=true
METRICS_EXPORT_ENABLED=true
```

### 2. Infrastructure Setup

#### Using Docker Compose
```bash
# Start infrastructure services
docker-compose up -d postgres redis elasticsearch jaeger zipkin

# Verify services are running
docker-compose ps

# Expected output:
# postgres        Up      5432/tcp
# redis          Up      6379/tcp
# elasticsearch  Up      9200/tcp
# jaeger         Up      14268/tcp, 16686/tcp
# zipkin         Up      9411/tcp
```

#### Docker Compose Configuration (docker-compose.yml)
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: tracing_config_db
      POSTGRES_USER: tracing_user
      POSTGRES_PASSWORD: tracing_secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tracing_user -d tracing_config_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:6.2-alpine
    ports:
      - "6379:6379"
    command: redis-server --requirepass redis_secure_password
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.6.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  jaeger:
    image: jaegertracing/all-in-one:1.42
    ports:
      - "16686:16686"  # Jaeger UI
      - "14268:14268"  # HTTP collector
      - "14250:14250"  # gRPC collector
      - "6831:6831/udp"    # UDP Thrift
      - "6832:6832/udp"    # UDP Thrift binary
    environment:
      COLLECTOR_OTLP_ENABLED: true

  zipkin:
    image: openzipkin/zipkin:2.24
    ports:
      - "9411:9411"
    environment:
      STORAGE_TYPE: elasticsearch
      ES_HOSTS: http://elasticsearch:9200

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:
```

### 3. Database Setup

#### Initialize Database
```bash
# Connect to PostgreSQL
psql -h localhost -U tracing_user -d tracing_config_db

# Run initialization script
\i database/migrations/V1__initial_schema.sql
\i database/seeds/V1__sample_data.sql

# Verify tables created
\dt
# Expected: tracing_configurations, sampling_policies, export_configurations
```

#### Database Migration (V1__initial_schema.sql)
```sql
-- Tracing Configurations Table
CREATE TABLE tracing_configurations (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    service_version VARCHAR(100) DEFAULT '1.0.0',
    sampling_strategy JSONB NOT NULL DEFAULT '{"type":"probabilistic","param":0.1}',
    export_config JSONB NOT NULL DEFAULT '{"jaeger":{"enabled":true},"zipkin":{"enabled":false}}',
    tags JSONB DEFAULT '{}',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_tracing_service UNIQUE (service_name, service_version)
);

-- Sampling Policies Table
CREATE TABLE sampling_policies (
    id BIGSERIAL PRIMARY KEY,
    policy_name VARCHAR(255) NOT NULL UNIQUE,
    policy_type VARCHAR(50) NOT NULL CHECK (policy_type IN ('probabilistic', 'ratelimiting', 'adaptive')),
    parameters JSONB NOT NULL,
    applies_to JSONB DEFAULT '{}',
    priority INTEGER DEFAULT 100,
    enabled BOOLEAN DEFAULT true,
    valid_from TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Export Configurations Table
CREATE TABLE export_configurations (
    id BIGSERIAL PRIMARY KEY,
    exporter_name VARCHAR(255) NOT NULL UNIQUE,
    exporter_type VARCHAR(50) NOT NULL CHECK (exporter_type IN ('jaeger', 'zipkin', 'otlp', 'elasticsearch')),
    endpoint_url VARCHAR(500) NOT NULL,
    authentication JSONB,
    configuration JSONB DEFAULT '{}',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_tracing_service_name ON tracing_configurations (service_name);
CREATE INDEX idx_tracing_enabled ON tracing_configurations (enabled);
CREATE INDEX idx_tracing_updated_at ON tracing_configurations (updated_at);
CREATE INDEX idx_sampling_policy_type ON sampling_policies (policy_type);
CREATE INDEX idx_sampling_enabled ON sampling_policies (enabled);
CREATE INDEX idx_export_type ON export_configurations (exporter_type);
CREATE INDEX idx_export_enabled ON export_configurations (enabled);

-- Audit trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tracing_configurations_updated_at 
    BEFORE UPDATE ON tracing_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sampling_policies_updated_at 
    BEFORE UPDATE ON sampling_policies 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_export_configurations_updated_at 
    BEFORE UPDATE ON export_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 4. Application Build and Run

#### Build Application
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package -DskipTests

# Verify JAR created
ls -la target/
# Expected: tracing-config-1.0.0.jar
```

#### Run Application
```bash
# Method 1: Using Maven
mvn spring-boot:run

# Method 2: Using Java directly
java -jar target/tracing-config-1.0.0.jar

# Method 3: Using Docker
docker build -t tracing-config:dev .
docker run -p 8093:8093 --env-file .env tracing-config:dev
```

#### Verify Application Startup
```bash
# Check health endpoint
curl http://localhost:8093/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}

# Check service info
curl http://localhost:8093/api/v1/info

# Expected response:
{
  "service": "tracing-config",
  "version": "1.0.0",
  "environment": "development",
  "uptime": "PT2M30S"
}
```

## Production Deployment

### 1. Container Registry Setup

#### Build and Push Docker Image
```bash
# Build production image
docker build -t exalt/tracing-config:1.0.0 .
docker build -t exalt/tracing-config:latest .

# Tag for registry
docker tag exalt/tracing-config:1.0.0 your-registry.com/exalt/tracing-config:1.0.0
docker tag exalt/tracing-config:latest your-registry.com/exalt/tracing-config:latest

# Push to registry
docker push your-registry.com/exalt/tracing-config:1.0.0
docker push your-registry.com/exalt/tracing-config:latest
```

#### Multi-stage Dockerfile (Production Optimized)
```dockerfile
# Multi-stage build for Tracing Configuration Service
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests -Dspring-boot.repackage.skip=false

# Runtime stage
FROM openjdk:17-jre-slim

# Install curl and other runtime dependencies
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    jq \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create non-root user for security
RUN groupadd -r tracing && useradd -r -g tracing tracing

# Copy application JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy configuration files
COPY src/main/resources/sampling_strategies.json /app/config/
COPY scripts/entrypoint.sh /app/
COPY scripts/healthcheck.sh /app/

# Create necessary directories and set permissions
RUN mkdir -p /app/logs /app/config /app/data && \
    chown -R tracing:tracing /app && \
    chmod +x /app/entrypoint.sh /app/healthcheck.sh

# Switch to non-root user
USER tracing

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /app/healthcheck.sh

# Expose ports
EXPOSE 8093 9090 14268 14250 6831 6832

# Set JVM options for production
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Run application with entrypoint script
ENTRYPOINT ["/app/entrypoint.sh"]
```

### 2. Kubernetes Deployment

#### Namespace and RBAC Setup
```bash
# Create namespace
kubectl create namespace exalt-shared

# Apply RBAC configuration
kubectl apply -f k8s/rbac.yaml
```

#### RBAC Configuration (k8s/rbac.yaml)
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tracing-config-sa
  namespace: exalt-shared
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: tracing-config-role
  namespace: exalt-shared
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list", "watch"]
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: tracing-config-rolebinding
  namespace: exalt-shared
subjects:
- kind: ServiceAccount
  name: tracing-config-sa
  namespace: exalt-shared
roleRef:
  kind: Role
  name: tracing-config-role
  apiGroup: rbac.authorization.k8s.io
```

#### Secrets Management
```bash
# Create database secret
kubectl create secret generic tracing-secrets \
  --from-literal=db-username=tracing_user \
  --from-literal=db-password=your-secure-password \
  --from-literal=jwt-secret=your-jwt-secret \
  --from-literal=redis-password=redis-password \
  --from-literal=elasticsearch-username=elastic \
  --from-literal=elasticsearch-password=elastic-password \
  -n exalt-shared

# Verify secret created
kubectl get secrets -n exalt-shared
```

#### ConfigMap Configuration
```bash
# Apply ConfigMap
kubectl apply -f k8s/configmap.yaml
```

#### ConfigMap (k8s/configmap.yaml)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tracing-config
  namespace: exalt-shared
data:
  application.yml: |
    server:
      port: 8093
      servlet:
        context-path: /
    spring:
      application:
        name: tracing-config
      profiles:
        active: ${SPRING_PROFILES_ACTIVE:production}
      datasource:
        url: jdbc:postgresql://${DB_HOST:postgres-service}:${DB_PORT:5432}/${DB_NAME:tracing_config_db}
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        hikari:
          maximum-pool-size: 20
          minimum-idle: 5
          connection-timeout: 30000
      redis:
        host: ${REDIS_HOST:redis-service}
        port: ${REDIS_PORT:6379}
        database: ${REDIS_DATABASE:12}
        password: ${REDIS_PASSWORD}
        timeout: 2000ms
        lettuce:
          pool:
            max-active: 8
            max-idle: 8
            min-idle: 0
      jpa:
        hibernate:
          ddl-auto: validate
        show-sql: false
        properties:
          hibernate:
            dialect: org.hibernate.dialect.PostgreSQLDialect
            format_sql: true
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
      metrics:
        export:
          prometheus:
            enabled: true
    logging:
      level:
        com.exalt: ${LOG_LEVEL:INFO}
        org.springframework.security: INFO
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
    tracing:
      jaeger:
        endpoint: ${JAEGER_ENDPOINT:http://jaeger-collector:14268/api/traces}
      zipkin:
        endpoint: ${ZIPKIN_ENDPOINT:http://zipkin:9411/api/v2/spans}
      sampling:
        default-probability: 0.01
        high-priority-probability: 1.0
      export:
        batch-timeout: 5s
        batch-size: 1000
        max-queue-size: 10000
  sampling_strategies.json: |
    {
      "default_strategy": {
        "type": "probabilistic",
        "param": 0.01
      },
      "per_service_strategies": [
        {
          "service": "user-management",
          "type": "probabilistic",
          "param": 0.1,
          "per_operation_strategies": [
            {
              "operation": "POST /api/v1/users",
              "type": "probabilistic",
              "param": 1.0
            }
          ]
        },
        {
          "service": "payment-service",
          "type": "probabilistic",
          "param": 1.0
        }
      ]
    }
```

#### Deploy Application
```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/

# Verify deployment
kubectl get pods -n exalt-shared -l app=tracing-config

# Check logs
kubectl logs -f deployment/tracing-config -n exalt-shared

# Port forward for testing
kubectl port-forward service/tracing-config 8093:8093 -n exalt-shared
```

### 3. Ingress Configuration

#### Ingress Setup (k8s/ingress.yaml)
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tracing-config-ingress
  namespace: exalt-shared
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - tracing-config.exalt.com
    secretName: tracing-config-tls
  rules:
  - host: tracing-config.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: tracing-config
            port:
              number: 8093
```

## Configuration Management

### 1. Environment-Specific Configurations

#### Development Environment
```yaml
# config/application-development.yml
spring:
  profiles: development
  
logging:
  level:
    com.exalt: DEBUG
    org.springframework.web: DEBUG
    
tracing:
  sampling:
    default-probability: 1.0  # 100% sampling in development
  export:
    batch-size: 10
    batch-timeout: 1s

management:
  endpoints:
    web:
      exposure:
        include: "*"  # All actuator endpoints in development
```

#### Staging Environment
```yaml
# config/application-staging.yml
spring:
  profiles: staging
  
logging:
  level:
    com.exalt: INFO
    
tracing:
  sampling:
    default-probability: 0.1  # 10% sampling in staging
  export:
    batch-size: 100
    batch-timeout: 2s
```

#### Production Environment
```yaml
# config/application-production.yml
spring:
  profiles: production
  
logging:
  level:
    com.exalt: WARN
    root: ERROR
    
tracing:
  sampling:
    default-probability: 0.01  # 1% sampling in production
  export:
    batch-size: 1000
    batch-timeout: 5s

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 2. Security Configuration

#### TLS/SSL Configuration
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tracing-config
  port: 8443
```

#### JWT Configuration
```yaml
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 3600  # 1 hour
    refresh-expiration: 86400  # 24 hours
    issuer: com.exalt.ecosystem
```

## Monitoring Setup

### 1. Prometheus Configuration

#### ServiceMonitor (k8s/servicemonitor.yaml)
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: tracing-config-monitor
  namespace: exalt-shared
  labels:
    app: tracing-config
spec:
  selector:
    matchLabels:
      app: tracing-config
  endpoints:
  - port: metrics
    interval: 30s
    path: /actuator/prometheus
```

### 2. Grafana Dashboard

#### Import Dashboard
```bash
# Download Grafana dashboard JSON
curl -o grafana-dashboard.json https://raw.githubusercontent.com/exalt/monitoring/main/dashboards/tracing-config.json

# Import via Grafana UI or API
curl -X POST \
  http://grafana.exalt.com/api/dashboards/db \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_API_KEY' \
  -d @grafana-dashboard.json
```

### 3. Alerting Rules

#### PrometheusRule (k8s/prometheusrule.yaml)
```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: tracing-config-alerts
  namespace: exalt-shared
spec:
  groups:
  - name: tracing-config
    rules:
    - alert: TracingConfigDown
      expr: up{job="tracing-config"} == 0
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "Tracing Config service is down"
        description: "Tracing Config service has been down for more than 5 minutes"
    
    - alert: TracingConfigHighLatency
      expr: histogram_quantile(0.95, http_request_duration_seconds_bucket{job="tracing-config"}) > 0.1
      for: 10m
      labels:
        severity: warning
      annotations:
        summary: "High latency in Tracing Config service"
        description: "95th percentile latency is above 100ms for 10 minutes"
```

## Validation and Testing

### 1. Health Check Validation
```bash
# Basic health check
curl -f http://localhost:8093/actuator/health || echo "Health check failed"

# Detailed health check
curl -s http://localhost:8093/actuator/health | jq .

# Database connectivity check
curl -s http://localhost:8093/actuator/health/db | jq .
```

### 2. API Endpoint Testing
```bash
# Test configuration retrieval
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8093/api/v1/config/services/user-management

# Test sampling strategy
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8093/api/v1/sampling/strategy/user-management

# Test export configuration
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8093/api/v1/export/configuration
```

### 3. Integration Testing
```bash
# Run integration tests
mvn test -Dtest=**/*IntegrationTest

# Run with specific profile
mvn test -Dspring.profiles.active=test -Dtest=**/*IntegrationTest

# Run with coverage
mvn test jacoco:report -Dtest=**/*IntegrationTest
```

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Application Won't Start
```bash
# Check Java version
java -version

# Check if port is already in use
netstat -tulpn | grep 8093

# Check environment variables
env | grep -E "(DB_|REDIS_|SPRING_)"

# Check logs for detailed error
tail -f logs/tracing-config.log
```

#### Issue 2: Database Connection Issues
```bash
# Test database connectivity
telnet postgres-service 5432

# Test with psql
psql -h postgres-service -U tracing_user -d tracing_config_db -c "SELECT 1;"

# Check database logs
kubectl logs -f deployment/postgres -n infrastructure
```

#### Issue 3: Redis Connection Issues
```bash
# Test Redis connectivity
redis-cli -h redis-service -p 6379 ping

# Test with authentication
redis-cli -h redis-service -p 6379 -a password ping

# Check Redis logs
kubectl logs -f deployment/redis -n infrastructure
```

### Performance Tuning

#### JVM Tuning
```bash
# Production JVM options
export JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseCGroupMemoryLimitForHeap \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Xloggc:logs/gc.log"
```

#### Database Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

## Next Steps

After successful setup, proceed to:

1. **Operations Guide**: [docs/operations/README.md](../operations/README.md)
2. **Architecture Documentation**: [docs/architecture/README.md](../architecture/README.md)
3. **API Documentation**: [api-docs/openapi.yaml](../../api-docs/openapi.yaml)

---

*This setup guide is maintained by the Shared Infrastructure Team. For issues or questions, contact infrastructure-team@exalt.com*