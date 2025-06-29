# Setup Guide - Admin Frameworks

## Overview

This document provides comprehensive setup instructions for the Admin Frameworks service, including local development environment, Docker setup, and cloud deployment.

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
- **Memory**: Minimum 8GB RAM (16GB recommended)
- **Storage**: Minimum 10GB free space
- **Network**: Stable internet connection

### Required Software

#### Development Tools

```bash
# Java Development Kit
Java 17+ (OpenJDK or Oracle JDK)

# Build Tool
Maven 3.8+

# IDE (Recommended)
IntelliJ IDEA or Visual Studio Code

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

#### Database and Messaging

```bash
# Database
PostgreSQL 14+

# Cache
Redis 6.2+

# Message Broker
Apache Kafka 3.0+
```

### Installation Instructions

#### Java 17

**Windows:**
```powershell
# Using Chocolatey
choco install openjdk17

# Or download from Oracle/OpenJDK website
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/admin-frameworks
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
SERVER_PORT=8400
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=admin_frameworks_db
DB_USER=admin_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Security
JWT_SECRET=your-secret-key
ADMIN_DEFAULT_PASSWORD=admin123
```

### 3. Infrastructure Setup

#### Option A: Docker Compose (Recommended)

```bash
# Start infrastructure services
docker-compose -f docker-compose.yml up -d postgres redis kafka

# Verify services are running
docker-compose ps
```

#### Option B: Native Installation

**PostgreSQL:**
```bash
# Create database
sudo -u postgres createdb admin_frameworks_db
sudo -u postgres psql -c "CREATE USER admin_user WITH PASSWORD 'secure_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE admin_frameworks_db TO admin_user;"
```

### 4. Build and Run

```bash
# Install dependencies
mvn clean install

# Run database migrations
mvn flyway:migrate

# Start the service
mvn spring-boot:run
```

### 5. Initialize Admin Components

```bash
# Run initialization script
./scripts/init-components.sh

# Or manually via API
curl -X POST http://localhost:8400/api/v1/admin/initialize \
  -H "Content-Type: application/json" \
  -d '{"adminPassword": "admin123"}'
```

## Docker Setup

### 1. Build Docker Image

```bash
# Build the image
docker build -t admin-frameworks:latest .

# Or use the build script
./scripts/build-docker.sh
```

### 2. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f admin-frameworks

# Scale the service
docker-compose up -d --scale admin-frameworks=3
```

### 3. Docker Configuration

```yaml
# docker-compose.yml
version: '3.8'
services:
  admin-frameworks:
    image: admin-frameworks:latest
    ports:
      - "8400:8400"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - redis
      - kafka
```

## Kubernetes Setup

### 1. Prerequisites

```bash
# Create namespace
kubectl create namespace shared-infrastructure

# Create secrets
kubectl create secret generic admin-frameworks-secrets \
  --from-literal=db-password=secure_password \
  --from-literal=jwt-secret=your-secret-key \
  -n shared-infrastructure
```

### 2. Deploy with Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/ -n shared-infrastructure

# Check deployment status
kubectl get pods -l app=admin-frameworks -n shared-infrastructure

# View logs
kubectl logs -f deployment/admin-frameworks -n shared-infrastructure
```

### 3. Helm Deployment

```bash
# Add Helm repository
helm repo add exalt https://charts.exalt.com
helm repo update

# Install the chart
helm install admin-frameworks exalt/admin-frameworks \
  --namespace shared-infrastructure \
  --set image.tag=latest \
  --set service.port=8400
```

## Configuration

### Application Configuration

```yaml
# application.yml
spring:
  application:
    name: admin-frameworks
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:admin_frameworks_db}
    username: ${DB_USER:admin_user}
    password: ${DB_PASSWORD:secure_password}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
server:
  port: ${SERVER_PORT:8400}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}

admin:
  security:
    jwt-secret: ${JWT_SECRET:default-secret}
    default-roles:
      - SUPER_ADMIN
      - ADMIN
      - VIEWER
```

### Component Configuration

```yaml
# admin-components.yml
components:
  dashboard:
    refresh-interval: 30s
    max-widgets: 20
    cache-ttl: 300s
  
  policy:
    evaluation-cache: true
    cache-size: 1000
    ttl: 600s
  
  reporting:
    max-concurrent-reports: 10
    export-formats:
      - PDF
      - EXCEL
      - CSV
    storage-path: /tmp/reports
```

## Verification

### 1. Health Checks

```bash
# Check service health
curl http://localhost:8400/actuator/health

# Check component status
curl http://localhost:8400/api/v1/admin/components/status

# Verify database connection
curl http://localhost:8400/actuator/health/db
```

### 2. Component Testing

```bash
# Test dashboard component
curl -X GET http://localhost:8400/api/v1/admin/dashboard/test \
  -H "Authorization: Bearer $TOKEN"

# Test policy engine
curl -X POST http://localhost:8400/api/v1/admin/policy/evaluate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"resource": "test", "action": "read"}'
```

### 3. Integration Testing

```bash
# Run integration tests
mvn verify -Pintegration-tests

# Run specific component tests
mvn test -Dtest=DashboardComponentTest
mvn test -Dtest=PolicyEngineTest
```

## Troubleshooting

### Common Issues

#### 1. Port Conflicts

**Problem**: Service fails to start due to port 8400 already in use

**Solution**:
```bash
# Find process using port
lsof -i :8400

# Change port in configuration
export SERVER_PORT=8401
```

#### 2. Database Connection Issues

**Problem**: Cannot connect to PostgreSQL

**Solutions**:
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Test connection
psql -h localhost -U admin_user -d admin_frameworks_db

# Check credentials
echo $DB_PASSWORD
```

#### 3. Component Initialization Failures

**Problem**: Admin components not initializing

**Solutions**:
```bash
# Check logs for errors
tail -f logs/admin-frameworks.log | grep ERROR

# Reinitialize components
curl -X POST http://localhost:8400/api/v1/admin/components/reinitialize

# Clear component cache
redis-cli FLUSHDB
```

#### 4. Memory Issues

**Problem**: OutOfMemoryError during report generation

**Solutions**:
```bash
# Increase heap size
export JAVA_OPTS="-Xmx2g -Xms1g"

# Or in application
java -Xmx2g -Xms1g -jar admin-frameworks.jar
```

### Debug Mode

```bash
# Enable debug logging
export LOG_LEVEL=DEBUG

# Or in application.yml
logging:
  level:
    com.exalt.admin: DEBUG
    org.springframework.security: DEBUG
```

### Performance Tuning

```bash
# JVM tuning for production
export JAVA_OPTS="
  -Xmx2g 
  -Xms2g 
  -XX:+UseG1GC 
  -XX:MaxGCPauseMillis=200 
  -XX:+HeapDumpOnOutOfMemoryError 
  -XX:HeapDumpPath=/var/log/admin-frameworks/
"
```

## Next Steps

1. **Configure Components**: Customize dashboard widgets and policies
2. **Set Up Monitoring**: Configure Prometheus and Grafana
3. **Enable Security**: Configure JWT and RBAC
4. **Create Admin Users**: Set up initial admin accounts
5. **Deploy to Production**: Follow production deployment guide

## Support

- **Documentation**: `/docs`
- **API Reference**: `http://localhost:8400/swagger-ui.html`
- **Issues**: GitHub Issues
- **Team Chat**: Slack #admin-frameworks
- **Email**: admin-team@exalt.com
