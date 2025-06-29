# Setup & Installation Guide - Geo-Location Service

## Overview

This guide provides comprehensive setup instructions for the Geo-Location Service, including development environment setup, configuration, deployment, and troubleshooting.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Development Environment Setup](#development-environment-setup)
3. [Configuration](#configuration)
4. [Database Setup](#database-setup)
5. [External API Setup](#external-api-setup)
6. [Building the Application](#building-the-application)
7. [Running the Service](#running-the-service)
8. [Docker Deployment](#docker-deployment)
9. [Kubernetes Deployment](#kubernetes-deployment)
10. [Monitoring Setup](#monitoring-setup)
11. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Java**: JDK 17 or higher
- **Maven**: 3.8.0 or higher
- **Database**: PostgreSQL 15+ with PostGIS extension
- **Cache**: Redis 7.0+
- **Memory**: Minimum 2GB RAM (4GB recommended)
- **CPU**: 2+ cores recommended
- **Storage**: 10GB available space

### Required Tools

```bash
# Java Development Kit
java -version  # Should be 17+

# Maven Build Tool
mvn -version   # Should be 3.8+

# Docker (for containerized deployment)
docker --version
docker-compose --version

# Kubernetes CLI (for K8s deployment)
kubectl version --client

# Database Tools
psql --version
```

### External Services

- **Mapping Providers**: At least one of:
  - Google Maps API (recommended)
  - OpenStreetMap/Nominatim
  - Here Maps API
  - Mapbox API
- **Service Discovery**: Eureka Server (for microservices)
- **API Gateway**: For external access
- **Monitoring**: Prometheus/Grafana (optional)

## Development Environment Setup

### 1. Clone the Repository

```bash
# Clone the main repository
git clone https://github.com/exalt-application-limited/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/geo-location-service
```

### 2. IDE Setup

#### IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Ensure Project SDK is set to Java 17
3. Enable annotation processing for Lombok
4. Install required plugins:
   - Lombok Plugin
   - Spring Boot Helper
   - Database Navigator

#### Visual Studio Code

1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure Java runtime to Java 17

### 3. Environment Variables

Create a `.env` file in the project root:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=geo_location_db
DB_USERNAME=geo_user
DB_PASSWORD=geo_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# External API Keys
GOOGLE_MAPS_API_KEY=your_google_maps_api_key
HERE_MAPS_API_KEY=your_here_maps_api_key
MAPBOX_ACCESS_TOKEN=your_mapbox_access_token

# Service Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development

# Eureka Configuration
EUREKA_SERVER_URL=http://localhost:8761/eureka
```

## Configuration

### Application Configuration

#### application.yml

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: geo-location-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:geo_location_db}
    username: ${DB_USERNAME:geo_user}
    password: ${DB_PASSWORD:geo_password}
    driver-class-name: org.postgresql.Driver
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect
        format_sql: true
  
  # Redis Configuration
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

# Eureka Configuration
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance-id:${random.value}}

# Management & Monitoring
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

# Logging Configuration
logging:
  level:
    com.exalt.ecosystem.shared.geolocation: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/geo-location-service.log
```

#### application-development.yml

```yaml
spring:
  # Development Database (can use H2 for testing)
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console

# Provider Configuration for Development
geo-location:
  providers:
    primary: local
    fallback: 
      - openstreetmap
    google-maps:
      api-key: ${GOOGLE_MAPS_API_KEY:}
      enabled: false
    here-maps:
      api-key: ${HERE_MAPS_API_KEY:}
      enabled: false
    mapbox:
      access-token: ${MAPBOX_ACCESS_TOKEN:}
      enabled: false
    openstreetmap:
      enabled: true
      base-url: https://nominatim.openstreetmap.org
    local:
      enabled: true
  
  # Cache Configuration
  cache:
    enabled: true
    ttl: 3600 # 1 hour
    max-size: 10000
  
  # Rate Limiting
  rate-limit:
    enabled: true
    requests-per-minute: 1000
    requests-per-hour: 10000
```

#### application-production.yml

```yaml
spring:
  # Production Database
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

# Provider Configuration for Production
geo-location:
  providers:
    primary: google-maps
    fallback: 
      - here-maps
      - openstreetmap
    google-maps:
      api-key: ${GOOGLE_MAPS_API_KEY}
      enabled: true
      requests-per-second: 50
      daily-quota: 100000
    here-maps:
      api-key: ${HERE_MAPS_API_KEY}
      enabled: true
      requests-per-second: 30
    mapbox:
      access-token: ${MAPBOX_ACCESS_TOKEN}
      enabled: true
    openstreetmap:
      enabled: true
      base-url: https://nominatim.openstreetmap.org
      requests-per-second: 1
  
  # Cache Configuration
  cache:
    enabled: true
    ttl: 86400 # 24 hours
    max-size: 100000
  
  # Rate Limiting
  rate-limit:
    enabled: true
    requests-per-minute: 1000
    requests-per-hour: 50000

# Logging for Production
logging:
  level:
    com.exalt.ecosystem.shared.geolocation: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
  file:
    name: /var/log/geo-location-service/application.log
    max-size: 100MB
    max-history: 30
```

## Database Setup

### PostgreSQL with PostGIS Setup

#### 1. Install PostgreSQL and PostGIS

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib postgis postgresql-15-postgis-3

# CentOS/RHEL
sudo yum install postgresql postgresql-server postgis

# macOS
brew install postgresql postgis

# Windows
# Download from https://www.postgresql.org/download/windows/
```

#### 2. Create Database and User

```sql
-- Connect as postgres user
sudo -u postgres psql

-- Create database and user
CREATE DATABASE geo_location_db;
CREATE USER geo_user WITH ENCRYPTED PASSWORD 'geo_password';
GRANT ALL PRIVILEGES ON DATABASE geo_location_db TO geo_user;

-- Connect to the database
\c geo_location_db;

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Verify PostGIS installation
SELECT PostGIS_version();
```

#### 3. Database Schema Migration

```bash
# Run Flyway migrations
mvn flyway:migrate

# Or run the application to auto-create schema
mvn spring-boot:run
```

### Database Schema

The service uses the following main tables:

```sql
-- Spatial reference table
CREATE TABLE spatial_ref_sys (
    srid integer NOT NULL,
    auth_name character varying(256),
    auth_srid integer,
    srtext character varying(2048),
    proj4text character varying(2048)
);

-- Locations table
CREATE TABLE locations (
    id bigserial PRIMARY KEY,
    latitude decimal(10,8) NOT NULL,
    longitude decimal(11,8) NOT NULL,
    altitude decimal(8,2),
    accuracy_meters integer,
    geom geometry(Point,4326),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Addresses table
CREATE TABLE addresses (
    id bigserial PRIMARY KEY,
    location_id bigint REFERENCES locations(id),
    formatted_address text NOT NULL,
    street_number varchar(20),
    street_name varchar(255),
    city varchar(100),
    state_province varchar(100),
    postal_code varchar(20),
    country varchar(100),
    country_code char(2),
    address_type varchar(50),
    validation_status varchar(20),
    quality_score integer,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Boundaries table
CREATE TABLE boundaries (
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL,
    type varchar(50) NOT NULL,
    parent_id bigint REFERENCES boundaries(id),
    geom geometry(MultiPolygon,4326) NOT NULL,
    properties jsonb,
    active boolean DEFAULT true,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Route cache table
CREATE TABLE route_cache (
    id bigserial PRIMARY KEY,
    origin_hash varchar(64) NOT NULL,
    destination_hash varchar(64) NOT NULL,
    travel_mode varchar(20) NOT NULL,
    route_data jsonb NOT NULL,
    distance_meters integer,
    duration_seconds integer,
    expires_at timestamp NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Geo requests audit table
CREATE TABLE geo_requests (
    id bigserial PRIMARY KEY,
    request_type varchar(50) NOT NULL,
    request_data jsonb,
    response_data jsonb,
    provider_used varchar(50),
    success boolean NOT NULL,
    processing_time_ms integer,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_locations_geom ON locations USING GIST (geom);
CREATE INDEX idx_boundaries_geom ON boundaries USING GIST (geom);
CREATE INDEX idx_addresses_postal_code ON addresses (postal_code);
CREATE INDEX idx_addresses_city ON addresses (city);
CREATE INDEX idx_route_cache_hash ON route_cache (origin_hash, destination_hash, travel_mode);
CREATE INDEX idx_locations_lat_lng ON locations (latitude, longitude);
CREATE INDEX idx_route_cache_expires ON route_cache (expires_at);
```

## External API Setup

### Google Maps API Setup

1. **Create Google Cloud Project**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing
   - Enable billing for the project

2. **Enable APIs**
   ```bash
   # Enable required APIs
   gcloud services enable geocoding.googleapis.com
   gcloud services enable directions.googleapis.com
   gcloud services enable distancematrix.googleapis.com
   gcloud services enable places.googleapis.com
   ```

3. **Create API Key**
   - Go to APIs & Services > Credentials
   - Create API Key
   - Restrict the key to specific APIs and IP addresses

4. **Set Environment Variable**
   ```bash
   export GOOGLE_MAPS_API_KEY="your_api_key_here"
   ```

### Here Maps API Setup

1. **Create Here Developer Account**
   - Go to [Here Developer Portal](https://developer.here.com/)
   - Create account and project

2. **Generate API Key**
   - Go to Projects > Your Project
   - Generate API Key
   - Note the API key

3. **Set Environment Variable**
   ```bash
   export HERE_MAPS_API_KEY="your_api_key_here"
   ```

### Mapbox Setup

1. **Create Mapbox Account**
   - Go to [Mapbox](https://www.mapbox.com/)
   - Create account

2. **Get Access Token**
   - Go to Account > Access Tokens
   - Copy the default public token or create a new one

3. **Set Environment Variable**
   ```bash
   export MAPBOX_ACCESS_TOKEN="your_access_token_here"
   ```

## Building the Application

### Maven Build

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the application
mvn clean package

# Skip tests (for faster builds)
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -Pproduction
```

### Build Verification

```bash
# Verify the JAR file
ls -la target/geo-location-service-*.jar

# Check if all dependencies are included
jar tf target/geo-location-service-*.jar | grep -E "(spring|hibernate|postgres)"
```

## Running the Service

### Development Mode

```bash
# Run with Maven
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Run with environment variables
SPRING_PROFILES_ACTIVE=development \
DB_HOST=localhost \
REDIS_HOST=localhost \
mvn spring-boot:run
```

### Production Mode

```bash
# Run JAR file
java -jar target/geo-location-service-1.0.0.jar

# Run with specific profile
java -jar -Dspring.profiles.active=production target/geo-location-service-1.0.0.jar

# Run with custom configuration
java -jar -Dserver.port=8081 -Dspring.profiles.active=production target/geo-location-service-1.0.0.jar
```

### Using Scripts

```bash
# Development script
./scripts/dev.sh

# Setup script (first time)
./scripts/setup.sh
```

## Docker Deployment

### Building Docker Image

```bash
# Build Docker image
docker build -t exalt/geo-location-service:latest .

# Build with specific tag
docker build -t exalt/geo-location-service:1.0.0 .

# Build with build args
docker build --build-arg SPRING_PROFILES_ACTIVE=production -t exalt/geo-location-service:prod .
```

### Running with Docker

```bash
# Run single container
docker run -d \
  --name geo-location-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_HOST=postgres \
  -e REDIS_HOST=redis \
  exalt/geo-location-service:latest

# Run with environment file
docker run -d \
  --name geo-location-service \
  -p 8080:8080 \
  --env-file .env \
  exalt/geo-location-service:latest
```

### Docker Compose

```yaml
version: '3.8'

services:
  geo-location-service:
    image: exalt/geo-location-service:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - GOOGLE_MAPS_API_KEY=${GOOGLE_MAPS_API_KEY}
    depends_on:
      - postgres
      - redis
    volumes:
      - ./logs:/var/log/geo-location-service
    restart: unless-stopped

  postgres:
    image: postgis/postgis:15-3.3
    environment:
      - POSTGRES_DB=geo_location_db
      - POSTGRES_USER=geo_user
      - POSTGRES_PASSWORD=geo_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

volumes:
  postgres_data:
  redis_data:
```

```bash
# Run with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f geo-location-service

# Stop services
docker-compose down
```

## Kubernetes Deployment

### Prerequisites

```bash
# Ensure kubectl is configured
kubectl cluster-info

# Create namespace
kubectl create namespace exalt-services
```

### ConfigMap

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: geo-location-config
  namespace: exalt-services
data:
  application.yml: |
    server:
      port: 8080
    spring:
      profiles:
        active: kubernetes
      datasource:
        url: jdbc:postgresql://postgres:5432/geo_location_db
        username: geo_user
        password: geo_password
      redis:
        host: redis
        port: 6379
    eureka:
      client:
        service-url:
          defaultZone: http://eureka-server:8761/eureka
```

### Secret

```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: geo-location-secret
  namespace: exalt-services
type: Opaque
data:
  google-maps-api-key: <base64-encoded-api-key>
  here-maps-api-key: <base64-encoded-api-key>
  mapbox-access-token: <base64-encoded-token>
```

### Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: geo-location-service
  namespace: exalt-services
spec:
  replicas: 3
  selector:
    matchLabels:
      app: geo-location-service
  template:
    metadata:
      labels:
        app: geo-location-service
    spec:
      containers:
      - name: geo-location-service
        image: exalt/geo-location-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: GOOGLE_MAPS_API_KEY
          valueFrom:
            secretKeyRef:
              name: geo-location-secret
              key: google-maps-api-key
        - name: HERE_MAPS_API_KEY
          valueFrom:
            secretKeyRef:
              name: geo-location-secret
              key: here-maps-api-key
        - name: MAPBOX_ACCESS_TOKEN
          valueFrom:
            secretKeyRef:
              name: geo-location-secret
              key: mapbox-access-token
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
      volumes:
      - name: config-volume
        configMap:
          name: geo-location-config
```

### Service

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: geo-location-service
  namespace: exalt-services
spec:
  selector:
    app: geo-location-service
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: ClusterIP
```

### HPA (Horizontal Pod Autoscaler)

```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: geo-location-hpa
  namespace: exalt-services
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: geo-location-service
  minReplicas: 2
  maxReplicas: 10
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
```

### Deployment Commands

```bash
# Apply configurations
kubectl apply -f k8s/

# Check deployment status
kubectl get deployments -n exalt-services
kubectl get pods -n exalt-services

# Check logs
kubectl logs -f deployment/geo-location-service -n exalt-services

# Scale deployment
kubectl scale deployment geo-location-service --replicas=5 -n exalt-services

# Port forward for testing
kubectl port-forward service/geo-location-service 8080:80 -n exalt-services
```

## Monitoring Setup

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'geo-location-service'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

### Grafana Dashboard

Key metrics to monitor:

- **Request Rate**: `rate(http_requests_total[5m])`
- **Response Time**: `histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))`
- **Error Rate**: `rate(http_requests_total{status=~"5.."}[5m])`
- **Cache Hit Rate**: `cache_hit_rate`
- **Provider Health**: `provider_health_status`

### Alerts

```yaml
# alerts.yml
groups:
  - name: geo-location-service
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues

**Problem**: Service fails to start with database connection errors

**Solutions**:
```bash
# Check database status
sudo systemctl status postgresql

# Verify database exists
sudo -u postgres psql -l | grep geo_location_db

# Test connection
psql -h localhost -U geo_user -d geo_location_db

# Check PostGIS extension
psql -h localhost -U geo_user -d geo_location_db -c "SELECT PostGIS_version();"
```

#### 2. API Key Issues

**Problem**: External API calls fail with authentication errors

**Solutions**:
```bash
# Verify API keys are set
echo $GOOGLE_MAPS_API_KEY
echo $HERE_MAPS_API_KEY

# Test Google Maps API
curl "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=$GOOGLE_MAPS_API_KEY"

# Check API quotas in respective consoles
```

#### 3. Redis Connection Issues

**Problem**: Caching doesn't work, Redis connection errors

**Solutions**:
```bash
# Check Redis status
redis-cli ping

# Test Redis connection
redis-cli -h localhost -p 6379 ping

# Check Redis logs
sudo journalctl -u redis
```

#### 4. High Memory Usage

**Problem**: Service consumes too much memory

**Solutions**:
```bash
# Check JVM memory settings
java -XX:+PrintFlagsFinal -version | grep HeapSize

# Adjust JVM settings
java -Xmx1g -Xms512m -jar geo-location-service.jar

# Monitor memory usage
jstat -gc <pid>
```

#### 5. Slow Response Times

**Problem**: API responses are slow

**Solutions**:
```bash
# Check database query performance
# Enable SQL logging and analyze slow queries

# Verify cache hit rates
curl http://localhost:8080/actuator/metrics/cache.hit.rate

# Check external provider response times
curl http://localhost:8080/actuator/metrics/provider.response.time
```

### Debugging Steps

1. **Check Application Logs**
   ```bash
   tail -f logs/geo-location-service.log
   ```

2. **Verify Health Endpoints**
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/actuator/info
   ```

3. **Test API Endpoints**
   ```bash
   # Test geocoding
   curl -X POST http://localhost:8080/api/v1/geo/geocode \
     -H "Content-Type: application/json" \
     -d '{"address": "1600 Amphitheatre Parkway, Mountain View, CA"}'
   
   # Test distance calculation
   curl "http://localhost:8080/api/v1/geo/distance?startLat=37.4224&startLng=-122.0842&endLat=37.4419&endLng=-122.1430"
   ```

4. **Check System Resources**
   ```bash
   # CPU and memory usage
   top -p $(pgrep -f geo-location-service)
   
   # Disk usage
   df -h
   
   # Network connections
   netstat -tlnp | grep 8080
   ```

### Performance Tuning

#### JVM Tuning

```bash
# Production JVM settings
JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
```

#### Database Tuning

```sql
-- PostgreSQL settings for better performance
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
SELECT pg_reload_conf();
```

#### Cache Optimization

```yaml
# Redis optimization
geo-location:
  cache:
    ttl: 86400  # 24 hours
    max-size: 100000
    eviction-policy: LRU
```

### Getting Help

- **Documentation**: Check the `/docs` folder for detailed documentation
- **API Documentation**: Visit `http://localhost:8080/swagger-ui.html`
- **Health Check**: Monitor `http://localhost:8080/actuator/health`
- **Metrics**: View metrics at `http://localhost:8080/actuator/metrics`
- **Logs**: Check application logs in the `logs/` directory

For additional support, contact the development team or create an issue in the project repository.

---

*This setup guide is maintained by the Exalt Application Limited development team. For questions or updates, please contact the development team.*