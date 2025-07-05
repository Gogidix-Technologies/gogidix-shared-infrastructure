# Translation Service - Setup Guide

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Configuration Management](#configuration-management)
- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Database Setup](#database-setup)
- [Translation Providers Setup](#translation-providers-setup)
- [Environment Validation](#environment-validation)
- [Troubleshooting](#troubleshooting)

## Overview

This guide provides comprehensive instructions for setting up the Translation Service in various environments, from local development to production Kubernetes clusters. The service is designed to be cloud-native and follows twelve-factor app principles for configuration and deployment.

### Deployment Environments

- **Development**: Local development with Docker Compose
- **Staging**: Kubernetes cluster with reduced resources
- **Production**: Highly available Kubernetes deployment with monitoring

## Prerequisites

### System Requirements

#### Minimum Hardware Requirements

```yaml
Development Environment:
  CPU: 2 cores
  Memory: 4GB RAM
  Storage: 10GB available space
  
Staging Environment:
  CPU: 4 cores
  Memory: 8GB RAM
  Storage: 50GB available space
  
Production Environment:
  CPU: 8+ cores per node
  Memory: 16GB+ RAM per node
  Storage: 100GB+ SSD storage
```

#### Software Dependencies

```bash
# Required Software
Java 17+ (OpenJDK recommended)
Maven 3.8+
Docker 20.10+
Docker Compose 2.0+
kubectl 1.25+ (for Kubernetes deployment)
Git 2.30+

# Optional Tools
PostgreSQL 14+ client tools
Redis CLI
Helm 3.10+ (for Kubernetes package management)
```

### Cloud Provider Requirements

#### AWS Setup

```bash
# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configure AWS credentials
aws configure set aws_access_key_id YOUR_ACCESS_KEY
aws configure set aws_secret_access_key YOUR_SECRET_KEY
aws configure set default.region us-east-1

# Verify AWS Translate access
aws translate list-terminologies
```

#### Google Cloud Setup

```bash
# Install Google Cloud SDK
curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-linux-x86_64.tar.gz
tar -xf google-cloud-cli-linux-x86_64.tar.gz
./google-cloud-sdk/install.sh

# Authenticate and set project
gcloud auth application-default login
gcloud config set project YOUR_PROJECT_ID

# Enable Translation API
gcloud services enable translate.googleapis.com

# Create service account and download key
gcloud iam service-accounts create translation-service
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
    --member="serviceAccount:translation-service@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/translate.user"
gcloud iam service-accounts keys create ~/translation-service-key.json \
    --iam-account=translation-service@YOUR_PROJECT_ID.iam.gserviceaccount.com
```

## Local Development Setup

### Quick Start

```bash
# 1. Clone the repository
git clone <repository-url>
cd shared-infrastructure/translation-service

# 2. Create environment configuration
cp .env.template .env

# 3. Start infrastructure services
docker-compose up -d postgres redis kafka

# 4. Wait for services to be ready
./scripts/wait-for-services.sh

# 5. Initialize database
mvn flyway:migrate

# 6. Start the application
mvn spring-boot:run
```

### Detailed Setup Process

#### 1. Environment Configuration

Create and configure your `.env` file:

```bash
# Copy template
cp .env.template .env

# Edit configuration
vim .env
```

```bash
# .env file contents
# Server Configuration
SERVER_PORT=8094
SPRING_PROFILES_ACTIVE=development

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=translation_service_db
DB_USERNAME=translation_user
DB_PASSWORD=secure_password_123

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=13
REDIS_PASSWORD=

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=translation-service-group

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Translation Provider API Keys
GOOGLE_TRANSLATE_API_KEY=your_google_api_key
MICROSOFT_TRANSLATOR_API_KEY=your_microsoft_api_key
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
DEEPL_API_KEY=your_deepl_api_key

# Security Configuration
JWT_SECRET=your_jwt_secret_key_here_min_256_bits
WEBHOOK_SECRET=your_webhook_secret

# Logging Configuration
LOG_LEVEL=INFO
LOG_FORMAT=JSON

# Monitoring Configuration
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

#### 2. Docker Compose for Development

```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: translation_service_db
      POSTGRES_USER: translation_user
      POSTGRES_PASSWORD: secure_password_123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U translation_user -d translation_service_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    healthcheck:
      test: ["CMD", "kafka-topics", "--bootstrap-server", "localhost:9092", "--list"]
      interval: 30s
      timeout: 10s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  eureka-server:
    image: springcloud/eureka
    ports:
      - "8761:8761"
    environment:
      - EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
      - EUREKA_CLIENT_FETCH_REGISTRY=false
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5

volumes:
  postgres_data:
  redis_data:
```

#### 3. Database Initialization

```bash
# Start database
docker-compose up -d postgres

# Wait for database to be ready
./scripts/wait-for-postgres.sh

# Run database migrations
mvn flyway:migrate

# Seed initial data (optional)
mvn flyway:migrate -Dflyway.locations=filesystem:database/seeds
```

#### 4. Application Startup

```bash
# Development mode with auto-reload
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=development"

# Or with specific profile
export SPRING_PROFILES_ACTIVE=development
mvn spring-boot:run

# Verify service is running
curl http://localhost:8094/actuator/health
```

### IDE Configuration

#### IntelliJ IDEA Setup

```bash
# 1. Import Maven project
File → Open → Select pom.xml

# 2. Configure JDK
File → Project Structure → Project → Project SDK → Java 17

# 3. Enable annotation processing
File → Settings → Build → Compiler → Annotation Processors → Enable

# 4. Install recommended plugins
- Lombok Plugin
- Spring Boot Assistant
- Docker Integration
- Kubernetes Plugin

# 5. Configure run configuration
Run → Edit Configurations → Add New → Spring Boot
Main class: com.exalt.ecosystem.shared.translation.TranslationServiceApplication
Environment variables: Load from .env file
```

#### VS Code Setup

```json
// .vscode/settings.json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "spring-boot.ls.checkupdates.on": true,
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
    "files.exclude": {
        "**/target": true,
        "**/.classpath": true,
        "**/.project": true,
        "**/.settings": true,
        "**/.factorypath": true
    }
}
```

```json
// .vscode/launch.json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Translation Service",
            "request": "launch",
            "mainClass": "com.exalt.ecosystem.shared.translation.TranslationServiceApplication",
            "envFile": "${workspaceFolder}/.env",
            "args": "--spring.profiles.active=development"
        }
    ]
}
```

## Configuration Management

### Environment-Specific Configuration

#### Development Configuration

```yaml
# src/main/resources/application-development.yml
server:
  port: 8094

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:translation_service_db}
    username: ${DB_USERNAME:translation_user}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    database: ${REDIS_DATABASE:13}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: ${KAFKA_GROUP_ID:translation-service-group}
      auto-offset-reset: earliest
    producer:
      acks: all
      retries: 3

logging:
  level:
    com.exalt.ecosystem.shared.translation: DEBUG
    org.springframework.security: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,configprops
  endpoint:
    health:
      show-details: always
```

#### Production Configuration

```yaml
# src/main/resources/application-production.yml
server:
  port: 8094
  tomcat:
    max-threads: 200
    min-spare-threads: 20

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    database: ${REDIS_DATABASE:13}
    password: ${REDIS_PASSWORD:}
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    consumer:
      group-id: ${KAFKA_GROUP_ID}
      auto-offset-reset: latest
      fetch-min-size: 1024
      fetch-max-wait: 500ms
    producer:
      acks: all
      retries: Integer.MAX_VALUE
      compression-type: snappy
      batch-size: 16384
      linger-ms: 5

logging:
  level:
    ROOT: INFO
    com.exalt.ecosystem.shared.translation: INFO
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### Secrets Management

#### Kubernetes Secrets

```bash
# Create secrets for production
kubectl create secret generic translation-secrets \
  --from-literal=db-username=translation_user \
  --from-literal=db-password=secure_production_password \
  --from-literal=jwt-secret=your_jwt_secret_key_here_min_256_bits \
  --from-literal=google-translate-api-key=your_google_api_key \
  --from-literal=microsoft-translator-api-key=your_microsoft_api_key \
  --from-literal=aws-access-key-id=your_aws_access_key \
  --from-literal=aws-secret-access-key=your_aws_secret_key \
  --from-literal=deepl-api-key=your_deepl_api_key \
  --from-literal=webhook-secret=your_webhook_secret \
  --namespace=exalt-shared
```

#### HashiCorp Vault Integration

```yaml
# vault-secrets.yml
apiVersion: v1
kind: Secret
metadata:
  name: vault-secrets
  annotations:
    vault.hashicorp.com/agent-inject: 'true'
    vault.hashicorp.com/role: 'translation-service'
    vault.hashicorp.com/agent-inject-secret-config: 'secret/data/translation-service'
type: Opaque
```

## Docker Deployment

### Docker Build Process

#### Multi-stage Dockerfile Optimization

```dockerfile
# Build stage
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Install additional packages
RUN apt-get update && \
    apt-get install -y \
        curl \
        jq \
        netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create non-root user
RUN groupadd -r translation && \
    useradd -r -g translation translation

# Copy application
COPY --from=builder /app/target/*.jar app.jar

# Copy configuration files
COPY --from=builder /app/src/main/resources/application*.yml ./config/

# Create necessary directories
RUN mkdir -p logs cache dictionaries && \
    chown -R translation:translation /app

# Switch to non-root user
USER translation

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8094/actuator/health || exit 1

# Expose ports
EXPOSE 8094

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose Production Setup

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  translation-service:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8094:8094"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    env_file:
      - .env.production
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      kafka:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8094/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: translation_service_db
      POSTGRES_USER: translation_user
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
    secrets:
      - postgres_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U translation_user -d translation_service_db"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes --requirepass "${REDIS_PASSWORD}"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "auth", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    restart: unless-stopped

secrets:
  postgres_password:
    file: ./secrets/postgres_password.txt

volumes:
  postgres_data:
  redis_data:

networks:
  default:
    driver: bridge
```

## Kubernetes Deployment

### Production-Ready Kubernetes Manifests

#### Namespace Configuration

```yaml
# k8s/00-namespace.yml
apiVersion: v1
kind: Namespace
metadata:
  name: exalt-shared
  labels:
    name: exalt-shared
    component: infrastructure
---
apiVersion: v1
kind: ResourceQuota
metadata:
  name: translation-service-quota
  namespace: exalt-shared
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 8Gi
    limits.cpu: "8"
    limits.memory: 16Gi
    persistentvolumeclaims: "4"
```

#### ConfigMap Configuration

```yaml
# k8s/01-configmap.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: translation-service-config
  namespace: exalt-shared
data:
  application.yml: |
    server:
      port: 8094
    spring:
      profiles:
        active: production
      kafka:
        bootstrap-servers: kafka-service:9092
        consumer:
          group-id: translation-service-group
        producer:
          acks: all
          retries: 3
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: when-authorized
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: translation-dictionaries
  namespace: exalt-shared
data:
  ecommerce-terms.json: |
    {
      "en-es": {
        "shopping cart": "carrito de compras",
        "checkout": "finalizar compra",
        "wishlist": "lista de deseos"
      },
      "en-fr": {
        "shopping cart": "panier",
        "checkout": "commande",
        "wishlist": "liste de souhaits"
      }
    }
```

#### Service Account and RBAC

```yaml
# k8s/02-rbac.yml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: translation-service
  namespace: exalt-shared
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: exalt-shared
  name: translation-service-role
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
  name: translation-service-binding
  namespace: exalt-shared
subjects:
- kind: ServiceAccount
  name: translation-service
  namespace: exalt-shared
roleRef:
  kind: Role
  name: translation-service-role
  apiGroup: rbac.authorization.k8s.io
```

#### Deployment Configuration

```yaml
# k8s/03-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: translation-service
  namespace: exalt-shared
  labels:
    app: translation-service
    component: infrastructure
    domain: shared
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: translation-service
  template:
    metadata:
      labels:
        app: translation-service
        component: infrastructure
        domain: shared
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8094"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: translation-service
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: translation-service
        image: exalt/translation-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8094
          name: http
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SERVER_PORT
          value: "8094"
        - name: DB_HOST
          value: "postgres-service"
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: "translation_db"
        - name: REDIS_HOST
          value: "redis-service"
        - name: REDIS_PORT
          value: "6379"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        - name: EUREKA_SERVER_URL
          value: "http://service-registry:8761/eureka"
        envFrom:
        - secretRef:
            name: translation-secrets
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8094
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8094
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8094
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 12
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: cache-volume
          mountPath: /app/cache
        - name: dictionaries-volume
          mountPath: /app/dictionaries
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
              - ALL
      volumes:
      - name: config-volume
        configMap:
          name: translation-service-config
      - name: cache-volume
        emptyDir:
          sizeLimit: 1Gi
      - name: dictionaries-volume
        configMap:
          name: translation-dictionaries
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
```

#### Service Configuration

```yaml
# k8s/04-service.yml
apiVersion: v1
kind: Service
metadata:
  name: translation-service
  namespace: exalt-shared
  labels:
    app: translation-service
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb
spec:
  type: ClusterIP
  ports:
  - port: 8094
    targetPort: 8094
    protocol: TCP
    name: http
  selector:
    app: translation-service
---
apiVersion: v1
kind: Service
metadata:
  name: translation-service-headless
  namespace: exalt-shared
  labels:
    app: translation-service
spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 8094
    targetPort: 8094
    protocol: TCP
    name: http
  selector:
    app: translation-service
```

#### Horizontal Pod Autoscaler

```yaml
# k8s/05-hpa.yml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: translation-service-hpa
  namespace: exalt-shared
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: translation-service
  minReplicas: 3
  maxReplicas: 20
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
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

#### Ingress Configuration

```yaml
# k8s/06-ingress.yml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: translation-service-ingress
  namespace: exalt-shared
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - translation-api.exalt.com
    secretName: translation-service-tls
  rules:
  - host: translation-api.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: translation-service
            port:
              number: 8094
```

### Deployment Scripts

#### Kubernetes Deployment Script

```bash
#!/bin/bash
# scripts/deploy-k8s.sh

set -e

NAMESPACE="exalt-shared"
DEPLOYMENT_NAME="translation-service"
IMAGE_TAG=${1:-latest}

echo "Deploying Translation Service to Kubernetes..."

# Validate kubectl connection
kubectl cluster-info

# Create namespace if it doesn't exist
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply configurations in order
echo "Applying configurations..."
kubectl apply -f k8s/00-namespace.yml
kubectl apply -f k8s/01-configmap.yml
kubectl apply -f k8s/02-rbac.yml

# Update image tag in deployment
sed "s|image: exalt/translation-service:.*|image: exalt/translation-service:$IMAGE_TAG|g" k8s/03-deployment.yml | kubectl apply -f -

kubectl apply -f k8s/04-service.yml
kubectl apply -f k8s/05-hpa.yml
kubectl apply -f k8s/06-ingress.yml

# Wait for deployment to be ready
echo "Waiting for deployment to be ready..."
kubectl rollout status deployment/$DEPLOYMENT_NAME -n $NAMESPACE --timeout=300s

# Verify deployment
echo "Verifying deployment..."
kubectl get pods -n $NAMESPACE -l app=$DEPLOYMENT_NAME
kubectl get svc -n $NAMESPACE -l app=$DEPLOYMENT_NAME

echo "Deployment completed successfully!"

# Show service endpoints
echo "Service endpoints:"
kubectl get ingress -n $NAMESPACE
```

## Database Setup

### PostgreSQL Configuration

#### Production Database Setup

```sql
-- Create database and user
CREATE DATABASE translation_service_db;
CREATE USER translation_user WITH PASSWORD 'secure_production_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE translation_service_db TO translation_user;
GRANT USAGE ON SCHEMA public TO translation_user;
GRANT CREATE ON SCHEMA public TO translation_user;

-- Create extensions
\c translation_service_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Set database parameters for performance
ALTER DATABASE translation_service_db SET work_mem = '256MB';
ALTER DATABASE translation_service_db SET maintenance_work_mem = '1GB';
ALTER DATABASE translation_service_db SET random_page_cost = 1.1;
ALTER DATABASE translation_service_db SET effective_cache_size = '8GB';
```

#### Connection Pool Configuration

```yaml
# PostgreSQL connection pooling with PgBouncer
# pgbouncer.ini
[databases]
translation_service_db = host=postgres-primary port=5432 dbname=translation_service_db

[pgbouncer]
listen_addr = 0.0.0.0
listen_port = 6432
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt
pool_mode = transaction
max_client_conn = 1000
default_pool_size = 50
reserve_pool_size = 10
reserve_pool_timeout = 5
```

### Database Migration with Flyway

#### Migration Configuration

```properties
# src/main/resources/db/migration/flyway.conf
flyway.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
flyway.user=${DB_USERNAME}
flyway.password=${DB_PASSWORD}
flyway.schemas=public
flyway.locations=filesystem:src/main/resources/db/migration
flyway.baseline-on-migrate=true
flyway.validate-on-migrate=true
```

#### Sample Migration Files

```sql
-- V001__Create_initial_schema.sql
CREATE TABLE translation_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_language VARCHAR(5) NOT NULL,
    target_languages TEXT[] NOT NULL,
    project_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID
);

CREATE INDEX idx_translation_projects_status ON translation_projects(status);
CREATE INDEX idx_translation_projects_type ON translation_projects(project_type);
```

```sql
-- V002__Create_translation_tables.sql
CREATE TABLE translation_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES translation_projects(id) ON DELETE CASCADE,
    source_content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    content_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
    quality_level VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_used VARCHAR(50),
    cost_amount DECIMAL(10,4),
    processing_time_ms INTEGER,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX idx_translation_requests_status ON translation_requests(status);
CREATE INDEX idx_translation_requests_hash_lang ON translation_requests(content_hash, source_language, target_language);
CREATE INDEX idx_translation_requests_project ON translation_requests(project_id);
```

## Translation Providers Setup

### Google Cloud Translation API

#### Service Account Setup

```bash
# Create service account
gcloud iam service-accounts create translation-service \
    --display-name="Translation Service Account"

# Assign roles
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:translation-service@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/translate.user"

# Create and download key
gcloud iam service-accounts keys create translation-service-key.json \
    --iam-account=translation-service@$PROJECT_ID.iam.gserviceaccount.com

# Set environment variable
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/translation-service-key.json
```

#### Configuration

```yaml
# Google Translate configuration
google:
  translate:
    api-key: ${GOOGLE_TRANSLATE_API_KEY}
    project-id: ${GOOGLE_CLOUD_PROJECT_ID}
    location: global
    model: nmt  # Neural Machine Translation
    format: text  # or html
    timeout: 30s
    retry:
      max-attempts: 3
      backoff-multiplier: 2
```

### AWS Translate Setup

#### IAM Role and Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "translate:TranslateText",
        "translate:TranslateDocument",
        "translate:ListTerminologies",
        "translate:GetTerminology"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::translation-documents/*"
    }
  ]
}
```

#### Configuration

```yaml
# AWS Translate configuration
aws:
  translate:
    region: ${AWS_REGION:us-east-1}
    access-key-id: ${AWS_ACCESS_KEY_ID}
    secret-access-key: ${AWS_SECRET_ACCESS_KEY}
    timeout: 30s
    custom-terminology:
      enabled: true
      terminology-names:
        - ecommerce-terms
        - legal-terms
```

### Microsoft Translator Setup

#### Configuration

```yaml
# Microsoft Translator configuration
microsoft:
  translator:
    api-key: ${MICROSOFT_TRANSLATOR_API_KEY}
    region: ${MICROSOFT_TRANSLATOR_REGION:global}
    endpoint: https://api.cognitive.microsofttranslator.com
    timeout: 30s
    profanity-action: NoAction
    profanity-marker: Asterisk
    include-alignment: false
    include-sentence-length: true
```

### DeepL API Setup

#### Configuration

```yaml
# DeepL configuration
deepl:
  api:
    auth-key: ${DEEPL_API_KEY}
    server-url: https://api-free.deepl.com  # or https://api.deepl.com for pro
    timeout: 30s
    formality: default  # less, more, default, prefer_less, prefer_more
    split-sentences: true
    preserve-formatting: true
    glossary:
      enabled: true
      default-glossary-id: ${DEEPL_DEFAULT_GLOSSARY_ID}
```

## Environment Validation

### Health Check Script

```bash
#!/bin/bash
# scripts/health-check.sh

set -e

SERVICE_URL=${1:-http://localhost:8094}
TIMEOUT=30

echo "Performing health check for Translation Service..."

# Check service health
echo "1. Checking service health endpoint..."
response=$(curl -s -w "%{http_code}" -o /tmp/health_response "$SERVICE_URL/actuator/health" --max-time $TIMEOUT)
http_code=$(tail -n1 <<< "$response")

if [ "$http_code" != "200" ]; then
    echo "❌ Health check failed with HTTP $http_code"
    cat /tmp/health_response
    exit 1
fi

echo "✅ Service health check passed"

# Check database connectivity
echo "2. Checking database connectivity..."
db_status=$(curl -s "$SERVICE_URL/actuator/health/db" --max-time $TIMEOUT | jq -r '.status')

if [ "$db_status" != "UP" ]; then
    echo "❌ Database health check failed"
    exit 1
fi

echo "✅ Database connectivity check passed"

# Check Redis connectivity
echo "3. Checking Redis connectivity..."
redis_status=$(curl -s "$SERVICE_URL/actuator/health/redis" --max-time $TIMEOUT | jq -r '.status')

if [ "$redis_status" != "UP" ]; then
    echo "❌ Redis health check failed"
    exit 1
fi

echo "✅ Redis connectivity check passed"

# Check translation providers
echo "4. Checking translation providers..."
providers_response=$(curl -s "$SERVICE_URL/api/v1/providers/status" --max-time $TIMEOUT)
available_providers=$(echo "$providers_response" | jq -r '.availableProviders | length')

if [ "$available_providers" -lt 1 ]; then
    echo "❌ No translation providers available"
    exit 1
fi

echo "✅ Translation providers check passed ($available_providers providers available)"

# Test basic translation
echo "5. Testing basic translation functionality..."
translation_test=$(curl -s -X POST "$SERVICE_URL/api/v1/translate/text" \
    -H "Content-Type: application/json" \
    -d '{"text": "Hello, World!", "sourceLang": "en", "targetLang": "es"}' \
    --max-time $TIMEOUT)

translation_result=$(echo "$translation_test" | jq -r '.translatedText')

if [ -z "$translation_result" ] || [ "$translation_result" = "null" ]; then
    echo "❌ Translation functionality test failed"
    exit 1
fi

echo "✅ Translation functionality test passed"
echo "   Original: Hello, World!"
echo "   Translated: $translation_result"

echo ""
echo "🎉 All health checks passed successfully!"
echo "Translation Service is ready for use."
```

### Validation Tests

```bash
#!/bin/bash
# scripts/validate-environment.sh

echo "Validating Translation Service environment..."

# Check required environment variables
required_vars=(
    "DB_HOST"
    "DB_PORT"
    "DB_NAME"
    "DB_USERNAME"
    "DB_PASSWORD"
    "REDIS_HOST"
    "REDIS_PORT"
    "GOOGLE_TRANSLATE_API_KEY"
)

missing_vars=()

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "❌ Missing required environment variables:"
    printf '%s\n' "${missing_vars[@]}"
    exit 1
fi

echo "✅ All required environment variables are set"

# Test database connection
echo "Testing database connection..."
if ! psql "postgresql://$DB_USERNAME:$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ Database connection failed"
    exit 1
fi

echo "✅ Database connection successful"

# Test Redis connection
echo "Testing Redis connection..."
if ! redis-cli -h $REDIS_HOST -p $REDIS_PORT ping > /dev/null 2>&1; then
    echo "❌ Redis connection failed"
    exit 1
fi

echo "✅ Redis connection successful"

echo "🎉 Environment validation completed successfully!"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Service Startup Issues

**Problem**: Service fails to start with port binding error

```bash
# Check if port is in use
sudo netstat -tlnp | grep :8094

# Kill process using the port
sudo kill -9 $(sudo lsof -t -i:8094)

# Or use a different port
export SERVER_PORT=8095
```

**Problem**: Database connection refused

```bash
# Check database status
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test connection manually
psql "postgresql://translation_user:password@localhost:5432/translation_service_db"
```

#### 2. Translation Provider Issues

**Problem**: Google Translate API authentication error

```bash
# Verify API key
curl -X POST \
  "https://translation.googleapis.com/language/translate/v2?key=$GOOGLE_TRANSLATE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"q": "Hello", "target": "es"}'

# Check service account key
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
gcloud auth application-default print-access-token
```

**Problem**: AWS Translate permission denied

```bash
# Verify AWS credentials
aws sts get-caller-identity

# Test translate service
aws translate translate-text \
  --text "Hello, World!" \
  --source-language-code en \
  --target-language-code es
```

#### 3. Performance Issues

**Problem**: High memory usage

```bash
# Monitor JVM memory
curl http://localhost:8094/actuator/metrics/jvm.memory.used

# Enable G1GC for better memory management
export JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Increase heap size if needed
export JAVA_OPTS="$JAVA_OPTS -Xmx4g"
```

**Problem**: Slow translation responses

```bash
# Check cache hit rate
curl http://localhost:8094/actuator/metrics/cache.gets

# Monitor translation provider response times
curl http://localhost:8094/actuator/metrics/translation.provider.response.time

# Enable connection pooling optimization
export SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=50
```

#### 4. Kubernetes Deployment Issues

**Problem**: Pod stuck in Pending state

```bash
# Check events
kubectl describe pod <pod-name> -n exalt-shared

# Check resource availability
kubectl describe nodes

# Check resource quotas
kubectl describe resourcequota -n exalt-shared
```

**Problem**: Service not accessible

```bash
# Check service endpoints
kubectl get endpoints translation-service -n exalt-shared

# Check ingress configuration
kubectl describe ingress translation-service-ingress -n exalt-shared

# Test service connectivity from within cluster
kubectl run test-pod --image=curlimages/curl -it --rm --restart=Never -- \
  curl http://translation-service:8094/actuator/health
```

### Debug Mode Setup

```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
export LOGGING_LEVEL_COM_GOGIDIX_ECOSYSTEM_SHARED_TRANSLATION=DEBUG

# Enable Spring Boot debug mode
export DEBUG=true

# Enable actuator debug endpoints
export MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*

# Start with remote debugging
export JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### Monitoring and Logging

```bash
# View application logs
tail -f logs/translation-service.log

# Monitor with journalctl (systemd)
journalctl -u translation-service -f

# Docker logs
docker logs -f translation-service

# Kubernetes logs
kubectl logs -f deployment/translation-service -n exalt-shared
```

This comprehensive setup guide covers all aspects of deploying and configuring the Translation Service from development to production environments. Follow the appropriate sections based on your deployment target and requirements.