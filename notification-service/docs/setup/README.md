# Setup Guide - Notification Service

## Overview

This guide provides comprehensive instructions for setting up the Notification Service, including local development, Docker deployment, Kubernetes installation, and external provider integrations.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Provider Configuration](#provider-configuration)
6. [Template Management](#template-management)
7. [Monitoring Setup](#monitoring-setup)
8. [Testing and Validation](#testing-and-validation)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **CPU**: Minimum 4 cores (8+ recommended for production)
- **RAM**: Minimum 8GB (16GB+ recommended for production)  
- **Storage**: 100GB+ SSD storage
- **Network**: Reliable internet for external provider APIs
- **OS**: Linux (Ubuntu 20.04+ or RHEL 8+)

### Software Requirements

```bash
# Java Development
- Java 17+ (OpenJDK recommended)
- Maven 3.8+

# Container Runtime
- Docker 24.0+
- Docker Compose 2.20+
- Kubernetes 1.28+ (for production)

# External Dependencies
- PostgreSQL 15+
- Redis 7.0+
- Apache Kafka 3.5+

# Development Tools
- Git 2.40+
- curl/httpie
- jq for JSON processing
```

### External Provider Accounts

```bash
# Email Providers
- SendGrid API Key
- AWS SES Credentials (optional)
- Mailgun API Key (optional)

# SMS Providers  
- Twilio Account SID & Auth Token
- AWS SNS Credentials (optional)
- MessageBird API Key (optional)

# Push Notification Providers
- Firebase Server Key
- Apple Push Notification Certificate
- OneSignal App ID & API Key (optional)
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt-tech/notification-service.git
cd notification-service
```

### 2. Environment Configuration

```bash
# Copy environment template
cp .env.template .env

# Edit configuration
nano .env
```

Required environment variables:
```env
# Application Configuration
SERVICE_NAME=notification-service
SERVICE_PORT=8087
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=notifications_db
DB_USERNAME=notifications_user
DB_PASSWORD=your_secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=notification-service
KAFKA_TOPICS=user.events,order.events,campaign.events

# Service Discovery
EUREKA_CLIENT_SERVICE_URL=http://localhost:8761/eureka/
EUREKA_INSTANCE_HOSTNAME=localhost

# Security Configuration
JWT_SECRET=your_jwt_secret_key_min_32_chars
ENCRYPTION_KEY=your_encryption_key_32_chars

# Email Provider Configuration
EMAIL_PROVIDER=sendgrid
SENDGRID_API_KEY=your_sendgrid_api_key
SENDGRID_FROM_EMAIL=noreply@example.com
SENDGRID_FROM_NAME=Exalt Platform

# AWS SES Configuration (optional)
AWS_SES_REGION=us-east-1
AWS_SES_ACCESS_KEY=your_aws_access_key
AWS_SES_SECRET_KEY=your_aws_secret_key

# SMS Provider Configuration
SMS_PROVIDER=twilio
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_FROM_NUMBER=+1234567890

# Push Notification Configuration
PUSH_PROVIDER=firebase
FIREBASE_SERVER_KEY=your_firebase_server_key
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-credentials.json

# Apple Push Notification
APNS_KEY_ID=your_apns_key_id
APNS_TEAM_ID=your_apns_team_id
APNS_BUNDLE_ID=com.exalt.app
APNS_CERTIFICATE_PATH=/path/to/apns-certificate.p8

# Rate Limiting Configuration
RATE_LIMIT_ENABLED=true
RATE_LIMIT_PER_USER_PER_MINUTE=60
RATE_LIMIT_PER_PROVIDER_PER_MINUTE=1000

# Template Configuration
TEMPLATE_CACHE_SIZE=1000
TEMPLATE_CACHE_TTL=3600

# Queue Configuration
QUEUE_BATCH_SIZE=100
QUEUE_RETRY_ATTEMPTS=3
QUEUE_RETRY_DELAY=5000

# Monitoring Configuration
METRICS_ENABLED=true
HEALTH_CHECK_ENABLED=true
ACTUATOR_ENDPOINTS=health,info,metrics,prometheus
```

### 3. Database Setup

```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Create database and user
sudo -u postgres psql

CREATE DATABASE notifications_db;
CREATE USER notifications_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE notifications_db TO notifications_user;

# Grant schema permissions
\c notifications_db
GRANT ALL ON SCHEMA public TO notifications_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO notifications_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO notifications_user;
```

### 4. Redis Setup

```bash
# Install and start Redis
sudo apt-get install redis-server
sudo systemctl start redis
sudo systemctl enable redis

# Configure Redis password
sudo nano /etc/redis/redis.conf
# Add: requirepass your_redis_password

# Restart Redis
sudo systemctl restart redis

# Test connection
redis-cli -a your_redis_password ping
```

### 5. Start Kafka

```bash
# Assuming Kafka is already running from message-broker setup
# Verify Kafka is running
kafka-topics --list --bootstrap-server localhost:9092

# Create notification topics if not exists
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic user.events \
  --partitions 3 \
  --replication-factor 1

kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic order.events \
  --partitions 3 \
  --replication-factor 1

kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic campaign.events \
  --partitions 3 \
  --replication-factor 1
```

### 6. Build and Run Application

```bash
# Install dependencies and build
mvn clean install

# Run database migrations
mvn flyway:migrate

# Start application
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/notification-service-1.0.0.jar
```

### 7. Verify Installation

```bash
# Check service health
curl http://localhost:8087/actuator/health

# Test notification API
curl -X POST http://localhost:8087/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "channel": "EMAIL",
    "recipient": "test@example.com",
    "templateId": "welcome-email",
    "variables": {
      "firstName": "John",
      "lastName": "Doe"
    }
  }'

# Check service registration
curl http://localhost:8761/eureka/apps/NOTIFICATION-SERVICE
```

## Docker Setup

### 1. Complete Docker Compose Configuration

```yaml
version: '3.8'

services:
  notification-service:
    build: .
    container_name: notification-service
    ports:
      - "8087:8087"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - EUREKA_CLIENT_SERVICE_URL=http://service-registry:8761/eureka/
    depends_on:
      - postgres
      - redis
      - kafka
      - service-registry
    volumes:
      - ./logs:/var/log/notification
      - ./templates:/var/notification/templates
    networks:
      - exalt-network
    restart: unless-stopped

  postgres:
    image: postgres:15-alpine
    container_name: notification-postgres
    environment:
      - POSTGRES_DB=notifications_db
      - POSTGRES_USER=notifications_user
      - POSTGRES_PASSWORD=your_secure_password
    volumes:
      - notification-postgres-data:/var/lib/postgresql/data
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - exalt-network
    restart: unless-stopped

  redis:
    image: redis:7.0-alpine
    container_name: notification-redis
    command: redis-server --requirepass your_redis_password
    volumes:
      - notification-redis-data:/data
    networks:
      - exalt-network
    restart: unless-stopped

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: notification-kafka
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
    networks:
      - exalt-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: notification-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - exalt-network

  notification-worker:
    build: .
    container_name: notification-worker
    environment:
      - SPRING_PROFILES_ACTIVE=worker
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    command: ["java", "-jar", "/app/notification-service.jar", "--worker.enabled=true"]
    depends_on:
      - postgres
      - redis
      - kafka
    volumes:
      - ./logs:/var/log/notification
    networks:
      - exalt-network
    restart: unless-stopped
    deploy:
      replicas: 3

volumes:
  notification-postgres-data:
  notification-redis-data:

networks:
  exalt-network:
    external: true
```

### 2. Dockerfile Configuration

```dockerfile
FROM openjdk:17-jdk-slim

# Create app directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src src

# Build application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Copy built JAR
RUN cp target/notification-service-*.jar notification-service.jar

# Create non-root user
RUN groupadd -r notification && useradd -r -g notification notification
RUN chown -R notification:notification /app

# Switch to non-root user
USER notification

# Expose port
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
```

### 3. Build and Run

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f notification-service

# Scale workers
docker-compose up -d --scale notification-worker=5

# Stop services
docker-compose down
```

## Kubernetes Deployment

### 1. Namespace and ConfigMaps

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: notification-system

---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: notification-config
  namespace: notification-system
data:
  application.yml: |
    spring:
      application:
        name: notification-service
      datasource:
        url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
      redis:
        host: ${REDIS_HOST}
        port: 6379
        password: ${REDIS_PASSWORD}
    kafka:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      consumer:
        group-id: ${KAFKA_GROUP_ID}
    notification:
      providers:
        email:
          sendgrid:
            api-key: ${SENDGRID_API_KEY}
            from-email: ${SENDGRID_FROM_EMAIL}
        sms:
          twilio:
            account-sid: ${TWILIO_ACCOUNT_SID}
            auth-token: ${TWILIO_AUTH_TOKEN}
            from-number: ${TWILIO_FROM_NUMBER}
        push:
          firebase:
            server-key: ${FIREBASE_SERVER_KEY}
    
  template-config.yml: |
    templates:
      cache:
        size: 1000
        ttl: 3600
      locales:
        - en
        - es
        - fr
        - de
```

### 2. Secrets

```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: notification-secrets
  namespace: notification-system
type: Opaque
stringData:
  db-password: your_secure_password
  redis-password: your_redis_password
  jwt-secret: your_jwt_secret
  sendgrid-api-key: your_sendgrid_api_key
  twilio-account-sid: your_twilio_account_sid
  twilio-auth-token: your_twilio_auth_token
  firebase-server-key: your_firebase_server_key
  encryption-key: your_encryption_key
```

### 3. Deployment

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
  namespace: notification-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
    spec:
      containers:
      - name: notification-service
        image: com.exalt/notification-service:latest
        ports:
        - containerPort: 8087
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DB_HOST
          value: "notification-postgres"
        - name: DB_NAME
          value: "notifications_db"
        - name: DB_USERNAME
          value: "notifications_user"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: db-password
        - name: REDIS_HOST
          value: "notification-redis"
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: redis-password
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        - name: SENDGRID_API_KEY
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: sendgrid-api-key
        - name: TWILIO_ACCOUNT_SID
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: twilio-account-sid
        - name: TWILIO_AUTH_TOKEN
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: twilio-auth-token
        - name: FIREBASE_SERVER_KEY
          valueFrom:
            secretKeyRef:
              name: notification-secrets
              key: firebase-server-key
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
            port: 8087
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8087
          initialDelaySeconds: 30
          periodSeconds: 5
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: template-volume
          mountPath: /app/templates
      volumes:
      - name: config-volume
        configMap:
          name: notification-config
      - name: template-volume
        persistentVolumeClaim:
          claimName: notification-templates-pvc

---
# worker-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-worker
  namespace: notification-system
spec:
  replicas: 5
  selector:
    matchLabels:
      app: notification-worker
  template:
    metadata:
      labels:
        app: notification-worker
    spec:
      containers:
      - name: notification-worker
        image: com.exalt/notification-service:latest
        command: ["java", "-jar", "/app/notification-service.jar"]
        args: ["--worker.enabled=true", "--api.enabled=false"]
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes,worker"
        # ... same environment variables as main service
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

### 4. Service Definitions

```yaml
# services.yaml
apiVersion: v1
kind: Service
metadata:
  name: notification-service
  namespace: notification-system
spec:
  selector:
    app: notification-service
  ports:
  - port: 8087
    targetPort: 8087
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: notification-postgres
  namespace: notification-system
spec:
  selector:
    app: notification-postgres
  ports:
  - port: 5432
    targetPort: 5432
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: notification-redis
  namespace: notification-system
spec:
  selector:
    app: notification-redis
  ports:
  - port: 6379
    targetPort: 6379
  type: ClusterIP
```

### 5. Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace notification-system

# Deploy ConfigMaps and Secrets
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# Deploy PostgreSQL and Redis
kubectl apply -f postgres-deployment.yaml
kubectl apply -f redis-deployment.yaml

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=notification-postgres -n notification-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=notification-redis -n notification-system --timeout=300s

# Deploy Notification Service
kubectl apply -f deployment.yaml
kubectl apply -f services.yaml

# Verify deployment
kubectl get all -n notification-system
```

## Provider Configuration

### 1. Email Provider Setup

#### SendGrid Configuration
```yaml
email:
  providers:
    sendgrid:
      api-key: ${SENDGRID_API_KEY}
      from-email: noreply@example.com
      from-name: Exalt Platform
      templates:
        welcome: d-1234567890abcdef
        password-reset: d-abcdef1234567890
```

#### AWS SES Configuration
```yaml
email:
  providers:
    aws-ses:
      region: us-east-1
      access-key: ${AWS_SES_ACCESS_KEY}
      secret-key: ${AWS_SES_SECRET_KEY}
      from-email: noreply@example.com
```

### 2. SMS Provider Setup

#### Twilio Configuration
```yaml
sms:
  providers:
    twilio:
      account-sid: ${TWILIO_ACCOUNT_SID}
      auth-token: ${TWILIO_AUTH_TOKEN}
      from-number: +1234567890
      webhook-url: https://api.example.com/notifications/webhooks/twilio
```

### 3. Push Notification Setup

#### Firebase Configuration
```yaml
push:
  providers:
    firebase:
      server-key: ${FIREBASE_SERVER_KEY}
      credentials-path: /path/to/firebase-credentials.json
      android:
        package-name: com.exalt.android
      ios:
        bundle-id: com.exalt.ios
```

#### Apple Push Notification Setup
```bash
# Generate APNS certificate
openssl pkcs8 -in AuthKey_XXXXXXXXXX.p8 -inform PEM -out apns-key.pem -outform PEM -nocrypt

# Configure APNS
apns:
  key-id: XXXXXXXXXX
  team-id: YYYYYYYYYY
  bundle-id: com.exalt.app
  key-path: /path/to/apns-key.pem
  environment: production  # or sandbox
```

## Template Management

### 1. Template Storage Setup

```bash
# Create template directory structure
mkdir -p /var/notification/templates/{email,sms,push}/{en,es,fr,de}

# Email templates
mkdir -p /var/notification/templates/email/welcome
mkdir -p /var/notification/templates/email/password-reset
mkdir -p /var/notification/templates/email/order-confirmation

# SMS templates
mkdir -p /var/notification/templates/sms/verification
mkdir -p /var/notification/templates/sms/order-status

# Push notification templates
mkdir -p /var/notification/templates/push/promotional
mkdir -p /var/notification/templates/push/transactional
```

### 2. Template Creation

#### Email Template Example
```html
<!-- /var/notification/templates/email/welcome/en/template.html -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome to Exalt Platform</title>
</head>
<body>
    <div style="max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;">
        <h1>Welcome {{firstName}}!</h1>
        <p>Thank you for joining the Exalt Platform. We're excited to have you on board.</p>
        <p>Your account has been successfully created with the email: {{email}}</p>
        <a href="{{confirmationUrl}}" style="background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
            Confirm Your Account
        </a>
    </div>
</body>
</html>
```

#### Template Metadata
```yaml
# /var/notification/templates/email/welcome/template.yml
name: welcome-email
channel: EMAIL
category: transactional
locale: en
variables:
  - name: firstName
    type: string
    required: true
  - name: email
    type: string
    required: true
  - name: confirmationUrl
    type: string
    required: true
subject: "Welcome to Exalt Platform, {{firstName}}!"
```

### 3. Template API Usage

```bash
# Create template via API
curl -X POST http://localhost:8087/api/v1/notifications/templates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "welcome-email",
    "channel": "EMAIL",
    "category": "transactional",
    "locale": "en",
    "subject": "Welcome to Exalt Platform, {{firstName}}!",
    "body": "<html>...</html>",
    "variables": [
      {"name": "firstName", "type": "string", "required": true},
      {"name": "email", "type": "string", "required": true}
    ]
  }'

# List templates
curl http://localhost:8087/api/v1/notifications/templates

# Get specific template
curl http://localhost:8087/api/v1/notifications/templates/welcome-email
```

## Monitoring Setup

### 1. Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'notification-service'
    static_configs:
      - targets: ['notification-service:8087']
    metrics_path: '/actuator/prometheus'
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        regex: '([^:]+):.*'
        replacement: '${1}'
```

### 2. Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Notification Service Monitoring",
    "panels": [
      {
        "title": "Notification Throughput",
        "targets": [
          {
            "expr": "sum(rate(notifications_sent_total[5m])) by (channel)",
            "legendFormat": "{{ channel }}"
          }
        ]
      },
      {
        "title": "Delivery Success Rate",
        "targets": [
          {
            "expr": "sum(rate(notifications_delivered_total[5m])) / sum(rate(notifications_sent_total[5m]))",
            "legendFormat": "Success Rate"
          }
        ]
      },
      {
        "title": "Queue Depth",
        "targets": [
          {
            "expr": "notification_queue_depth",
            "legendFormat": "{{ queue }}"
          }
        ]
      },
      {
        "title": "Provider Response Time",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, provider_response_time_seconds)",
            "legendFormat": "{{ provider }} 95th percentile"
          }
        ]
      }
    ]
  }
}
```

## Testing and Validation

### 1. Health Check Validation

```bash
# Check service health
curl http://localhost:8087/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "kafka": {"status": "UP"},
    "emailProvider": {"status": "UP"},
    "smsProvider": {"status": "UP"},
    "pushProvider": {"status": "UP"}
  }
}
```

### 2. API Testing

```bash
# Test email notification
curl -X POST http://localhost:8087/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "channel": "EMAIL",
    "recipient": "test@example.com",
    "templateId": "welcome-email",
    "variables": {
      "firstName": "John",
      "email": "test@example.com",
      "confirmationUrl": "https://example.com/confirm"
    }
  }'

# Test SMS notification
curl -X POST http://localhost:8087/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "channel": "SMS",
    "recipient": "+1234567890",
    "templateId": "verification-sms",
    "variables": {
      "code": "123456"
    }
  }'

# Test bulk notifications
curl -X POST http://localhost:8087/api/v1/notifications/bulk \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "notifications": [
      {
        "channel": "EMAIL",
        "recipient": "user1@example.com",
        "templateId": "promotional-email",
        "variables": {"name": "User1"}
      },
      {
        "channel": "EMAIL", 
        "recipient": "user2@example.com",
        "templateId": "promotional-email",
        "variables": {"name": "User2"}
      }
    ]
  }'
```

### 3. Integration Testing

```bash
# Test Kafka integration
kafka-console-producer --bootstrap-server localhost:9092 --topic user.events

# Send test event
{
  "eventType": "USER_REGISTERED",
  "userId": "12345",
  "email": "test@example.com",
  "firstName": "John",
  "timestamp": "2024-01-15T10:30:00Z"
}

# Verify notification was triggered and sent
curl http://localhost:8087/api/v1/notifications/history/12345
```

## Troubleshooting

### Common Issues

#### 1. Provider Authentication Errors
```bash
# Check provider configuration
curl http://localhost:8087/actuator/health/emailProvider

# Verify API keys
curl -X POST http://localhost:8087/api/v1/notifications/providers/test \
  -H "Content-Type: application/json" \
  -d '{"provider": "sendgrid"}'
```

#### 2. Template Rendering Issues
```bash
# Test template rendering
curl -X POST http://localhost:8087/api/v1/notifications/templates/test \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "welcome-email",
    "variables": {"firstName": "Test", "email": "test@example.com"}
  }'
```

#### 3. Queue Processing Issues
```bash
# Check queue status
redis-cli -a your_redis_password

# View queue contents
LRANGE notification:email:high 0 -1
LRANGE notification:sms:normal 0 -1

# Check queue processing metrics
curl http://localhost:8087/actuator/metrics/notification.queue.processed
```

#### 4. Database Connection Issues
```bash
# Test database connection
psql -h localhost -U notifications_user -d notifications_db

# Check connection pool
curl http://localhost:8087/actuator/metrics/hikaricp.connections.active
```

### Debug Commands

```bash
# View application logs
tail -f logs/notification-service.log

# Check specific log levels
grep "ERROR" logs/notification-service.log
grep "WARN" logs/notification-service.log

# Monitor real-time metrics
watch -n 1 'curl -s http://localhost:8087/actuator/metrics/notifications.sent.total | jq'

# Check template cache
curl http://localhost:8087/actuator/caches/templates
```

## Performance Optimization

### 1. Database Optimization

```sql
-- Create indexes for better performance
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_templates_channel_category ON templates(channel, category);
```

### 2. Redis Optimization

```bash
# Configure Redis for optimal performance
redis-cli CONFIG SET maxmemory 2gb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
redis-cli CONFIG SET save "900 1 300 10 60 10000"
```

### 3. Application Tuning

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      
  redis:
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        max-wait: 2000ms

notification:
  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 1000
  cache:
    template-cache-size: 1000
    template-cache-ttl: 3600
```

## Security Hardening

### 1. API Security

```yaml
# Enable HTTPS
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12

# Rate limiting
rate-limit:
  enabled: true
  requests-per-minute: 60
  burst-capacity: 100
```

### 2. Data Protection

```yaml
# Enable encryption
notification:
  encryption:
    enabled: true
    algorithm: AES-256-GCM
    key: ${ENCRYPTION_KEY}
  
  data-protection:
    mask-personal-data: true
    audit-access: true
    retention-days: 90
```

## Next Steps

1. Configure external provider accounts
2. Set up monitoring and alerting
3. Create notification templates
4. Configure user preferences
5. Set up A/B testing framework
6. Implement compliance reporting
7. Configure backup and recovery
8. Performance testing and optimization

For production deployment, ensure you have:
- Valid SSL certificates
- Proper firewall configuration
- Backup and recovery procedures
- Monitoring and alerting setup
- Compliance documentation
- Security audit completed