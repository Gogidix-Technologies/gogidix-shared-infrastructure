# User Profile Service - Setup Guide

## Overview

This guide provides comprehensive instructions for setting up, configuring, and deploying the User Profile Service in various environments. The service is built with Spring Boot 3.1.5 and requires Java 17.

## Prerequisites

### System Requirements

- **Java**: OpenJDK 17 or Oracle JDK 17+
- **Maven**: 3.8.0+
- **Docker**: 20.10.0+ with Docker Compose
- **Kubernetes**: 1.24+ (for production deployment)
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: 10GB free space (development), 50GB+ (production)

### External Dependencies

- **PostgreSQL**: 13.0+ (primary database)
- **Redis**: 6.2+ (caching and session storage)
- **Elasticsearch**: 8.0+ (search functionality)
- **AWS S3**: Compatible storage (avatar and file storage)

## Development Environment Setup

### 1. Local Development with Docker Compose

#### Clone and Setup

```bash
# Clone the repository
git clone <repository-url>
cd user-profile-service

# Make scripts executable
chmod +x scripts/*.sh

# Run setup script
./scripts/setup.sh
```

#### Docker Compose Configuration

Create `docker-compose.dev.yml`:

```yaml
version: '3.8'

services:
  user-profile-service:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8095:8095"
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=user_profile_dev
      - DB_USERNAME=userprofile
      - DB_PASSWORD=dev_password_123
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_DATABASE=14
      - ELASTICSEARCH_HOST=elasticsearch
      - ELASTICSEARCH_PORT=9200
      - AWS_ACCESS_KEY_ID=minioadmin
      - AWS_SECRET_ACCESS_KEY=minioadmin
      - AWS_S3_ENDPOINT=http://minio:9000
      - AWS_S3_BUCKET=user-profiles
      - JWT_SECRET=dev_jwt_secret_key_minimum_256_bits_required_for_security
      - EUREKA_SERVER_URL=http://service-registry:8761/eureka
    depends_on:
      - postgres
      - redis
      - elasticsearch
      - minio
    volumes:
      - ./logs:/app/logs
      - user-avatars:/var/user-profiles/avatars
    networks:
      - user-profile-network

  postgres:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=user_profile_dev
      - POSTGRES_USER=userprofile
      - POSTGRES_PASSWORD=dev_password_123
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/migrations:/docker-entrypoint-initdb.d
    networks:
      - user-profile-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --requirepass dev_redis_password
    volumes:
      - redis_data:/data
    networks:
      - user-profile-network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms1g -Xmx1g
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - user-profile-network

  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      - MINIO_ROOT_USER=minioadmin
      - MINIO_ROOT_PASSWORD=minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data
    networks:
      - user-profile-network

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:
  minio_data:
  user-avatars:

networks:
  user-profile-network:
    driver: bridge
```

#### Start Development Environment

```bash
# Start all services
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f user-profile-service

# Stop services
docker-compose -f docker-compose.dev.yml down
```

### 2. IDE Setup

#### IntelliJ IDEA Configuration

1. **Import Project**
   ```
   File → Open → Select pom.xml
   ```

2. **Configure JDK**
   ```
   File → Project Structure → Project → SDK → Java 17
   ```

3. **Enable Annotation Processing**
   ```
   Settings → Build → Compiler → Annotation Processors → Enable
   ```

4. **Install Required Plugins**
   - Lombok Plugin
   - MapStruct Support
   - Docker Plugin

#### VS Code Configuration

Create `.vscode/settings.json`:

```json
{
  "java.home": "/path/to/java17",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/path/to/java17"
    }
  ],
  "spring-boot.ls.java.home": "/path/to/java17",
  "java.compile.nullAnalysis.mode": "automatic"
}
```

### 3. Application Configuration

#### Development Properties

Create `src/main/resources/application-development.yml`:

```yaml
server:
  port: 8095
  servlet:
    context-path: /api/v1

spring:
  application:
    name: user-profile-service
  
  profiles:
    active: development
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:user_profile_dev}
    username: ${DB_USERNAME:userprofile}
    password: ${DB_PASSWORD:dev_password_123}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:dev_redis_password}
    database: ${REDIS_DATABASE:14}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 8
        min-idle: 2
  
  elasticsearch:
    uris: http://${ELASTICSEARCH_HOST:localhost}:${ELASTICSEARCH_PORT:9200}
    connection-timeout: 5s
    socket-timeout: 60s
  
  security:
    oauth2:
      client:
        registration:
          facebook:
            client-id: ${FACEBOOK_APP_ID:dev_facebook_app_id}
            client-secret: ${FACEBOOK_APP_SECRET:dev_facebook_app_secret}
            scope: email,public_profile
          google:
            client-id: ${GOOGLE_CLIENT_ID:dev_google_client_id}
            client-secret: ${GOOGLE_CLIENT_SECRET:dev_google_client_secret}
            scope: email,profile
          twitter:
            client-id: ${TWITTER_API_KEY:dev_twitter_api_key}
            client-secret: ${TWITTER_API_SECRET:dev_twitter_api_secret}
          linkedin:
            client-id: ${LINKEDIN_CLIENT_ID:dev_linkedin_client_id}
            client-secret: ${LINKEDIN_CLIENT_SECRET:dev_linkedin_client_secret}
            scope: r_liteprofile,r_emailaddress

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
    com.exalt.ecosystem.shared.userprofileservice: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/user-profile-service.log
    max-size: 100MB
    max-history: 30

app:
  jwt:
    secret: ${JWT_SECRET:dev_jwt_secret_key_minimum_256_bits_required_for_security}
    expiration: 86400000 # 24 hours
  
  aws:
    access-key-id: ${AWS_ACCESS_KEY_ID:minioadmin}
    secret-access-key: ${AWS_SECRET_ACCESS_KEY:minioadmin}
    region: ${AWS_REGION:us-east-1}
    s3:
      endpoint: ${AWS_S3_ENDPOINT:http://localhost:9000}
      bucket: ${AWS_S3_BUCKET:user-profiles}
      path-style-access: true
  
  file-upload:
    max-file-size: 10MB
    max-request-size: 10MB
    upload-dir: /var/user-profiles/avatars
    allowed-extensions: jpg,jpeg,png,gif,webp
  
  privacy:
    gdpr:
      enabled: true
      data-retention-days: 2555 # 7 years
      export-format: json
    ccpa:
      enabled: true
      opt-out-enabled: true
  
  eureka:
    client:
      service-url:
        defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
    instance:
      prefer-ip-address: true
```

## Production Environment Setup

### 1. Kubernetes Deployment

#### Prerequisites

```bash
# Create namespace
kubectl create namespace exalt-shared

# Create secrets
kubectl create secret generic user-profile-secrets \
  --from-literal=db-username="prod_user" \
  --from-literal=db-password="secure_prod_password" \
  --from-literal=jwt-secret="production_jwt_secret_key_256_bits_minimum" \
  --from-literal=aws-access-key-id="AKIA..." \
  --from-literal=aws-secret-access-key="..." \
  --from-literal=facebook-app-secret="..." \
  --from-literal=google-client-secret="..." \
  --from-literal=twitter-api-secret="..." \
  --from-literal=linkedin-client-secret="..." \
  --namespace=exalt-shared
```

#### Production Configuration

Create `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-profile-config
  namespace: exalt-shared
data:
  application-production.yml: |
    server:
      port: 8095
      shutdown: graceful
      tomcat:
        max-threads: 200
        min-spare-threads: 20
    
    spring:
      application:
        name: user-profile-service
      
      datasource:
        url: jdbc:postgresql://postgres-service:5432/user_profile_prod
        hikari:
          maximum-pool-size: 50
          minimum-idle: 10
          idle-timeout: 600000
          max-lifetime: 1800000
      
      jpa:
        hibernate:
          ddl-auto: validate
        show-sql: false
        properties:
          hibernate:
            jdbc:
              batch_size: 25
            order_inserts: true
            order_updates: true
      
      redis:
        host: redis-service
        port: 6379
        database: 14
        lettuce:
          pool:
            max-active: 50
            max-idle: 20
      
      elasticsearch:
        uris: http://elasticsearch-service:9200
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      metrics:
        export:
          prometheus:
            enabled: true
    
    logging:
      level:
        com.exalt.ecosystem.shared.userprofileservice: INFO
        org.springframework.security: WARN
      file:
        name: /app/logs/user-profile-service.log
    
    app:
      aws:
        region: us-east-1
        s3:
          bucket: exalt-user-profiles-prod
      
      privacy:
        gdpr:
          enabled: true
          data-retention-days: 2555
        ccpa:
          enabled: true
```

#### Persistent Volume Claims

Create `k8s/pvc.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: user-avatars-pvc
  namespace: exalt-shared
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 100Gi
  storageClassName: aws-efs
```

#### Production Deployment

```bash
# Apply configurations
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml

# Verify deployment
kubectl get pods -n exalt-shared -l app=user-profile-service
kubectl get svc -n exalt-shared user-profile-service
```

### 2. Database Setup

#### PostgreSQL Production Setup

```sql
-- Create database and user
CREATE DATABASE user_profile_prod;
CREATE USER user_profile_prod WITH ENCRYPTED PASSWORD 'secure_prod_password';
GRANT ALL PRIVILEGES ON DATABASE user_profile_prod TO user_profile_prod;

-- Connect to the database
\c user_profile_prod;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO user_profile_prod;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO user_profile_prod;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO user_profile_prod;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
```

#### Database Migration

```bash
# Run Flyway migrations (if using Flyway)
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/user_profile_prod \
                   -Dflyway.user=user_profile_prod \
                   -Dflyway.password=secure_prod_password

# Or use Liquibase (if using Liquibase)
mvn liquibase:update -Dliquibase.url=jdbc:postgresql://localhost:5432/user_profile_prod \
                     -Dliquibase.username=user_profile_prod \
                     -Dliquibase.password=secure_prod_password
```

### 3. Monitoring Setup

#### Prometheus Configuration

Create `monitoring/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'user-profile-service'
    static_configs:
      - targets: ['user-profile-service:9090']
    scrape_interval: 15s
    metrics_path: /actuator/prometheus
```

#### Grafana Dashboard

Create `monitoring/grafana-dashboard.json`:

```json
{
  "dashboard": {
    "title": "User Profile Service",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "http_server_requests_seconds{quantile=\"0.95\"}"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{status=~\"4..|5..\"}[5m])"
          }
        ]
      }
    ]
  }
}
```

## Configuration Management

### Environment Variables

#### Required Environment Variables

```bash
# Database Configuration
DB_HOST=postgres-service
DB_PORT=5432
DB_NAME=user_profile_prod
DB_USERNAME=user_profile_prod
DB_PASSWORD=secure_prod_password

# Cache Configuration
REDIS_HOST=redis-service
REDIS_PORT=6379
REDIS_PASSWORD=secure_redis_password
REDIS_DATABASE=14

# Search Configuration
ELASTICSEARCH_HOST=elasticsearch-service
ELASTICSEARCH_PORT=9200

# AWS Configuration
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
AWS_S3_BUCKET=exalt-user-profiles-prod

# Security Configuration
JWT_SECRET=production_jwt_secret_key_256_bits_minimum

# Social Login Configuration
FACEBOOK_APP_ID=...
FACEBOOK_APP_SECRET=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
TWITTER_API_KEY=...
TWITTER_API_SECRET=...
LINKEDIN_CLIENT_ID=...
LINKEDIN_CLIENT_SECRET=...

# Service Discovery
EUREKA_SERVER_URL=http://service-registry:8761/eureka

# External Services
AUTH_SERVICE_URL=http://auth-service:8080
NOTIFICATION_SERVICE_URL=http://notification-service:8091
KYC_SERVICE_URL=http://kyc-service:8088
FILE_STORAGE_SERVICE_URL=http://file-storage-service:8086
```

#### Optional Environment Variables

```bash
# Application Configuration
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8095
LOGGING_LEVEL_ROOT=INFO

# Performance Tuning
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"
HIKARI_MAXIMUM_POOL_SIZE=50
REDIS_POOL_MAX_ACTIVE=50

# Security Settings
CORS_ALLOWED_ORIGINS=https://app.exalt.com,https://admin.exalt.com
CSRF_ENABLED=true

# Feature Flags
GDPR_COMPLIANCE_ENABLED=true
CCPA_COMPLIANCE_ENABLED=true
AUDIT_LOGGING_ENABLED=true
```

### Configuration Validation

Create `scripts/validate-config.sh`:

```bash
#!/bin/bash

# Validate required environment variables
required_vars=(
    "DB_HOST" "DB_USERNAME" "DB_PASSWORD"
    "REDIS_HOST" "JWT_SECRET"
    "AWS_ACCESS_KEY_ID" "AWS_SECRET_ACCESS_KEY"
)

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "ERROR: Required environment variable $var is not set"
        exit 1
    fi
done

# Validate database connectivity
pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USERNAME
if [ $? -ne 0 ]; then
    echo "ERROR: Cannot connect to PostgreSQL database"
    exit 1
fi

# Validate Redis connectivity
redis-cli -h $REDIS_HOST -p $REDIS_PORT ping
if [ $? -ne 0 ]; then
    echo "ERROR: Cannot connect to Redis"
    exit 1
fi

echo "Configuration validation successful"
```

## Testing Setup

### Unit Testing

```bash
# Run unit tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=UserProfileServiceTest
```

### Integration Testing

```bash
# Run integration tests
mvn verify -P integration-tests

# Run with test containers
mvn verify -P integration-tests -Dspring.profiles.active=test-containers
```

### Load Testing

Create `load-test/k6-script.js`:

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '5m', target: 200 },
    { duration: '2m', target: 0 },
  ],
};

export default function () {
  let response = http.get('http://user-profile-service:8095/api/v1/profiles/health');
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  sleep(1);
}
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues

```bash
# Check database connectivity
kubectl exec -it deployment/user-profile-service -- sh -c "
  java -cp /app/lib/* org.postgresql.util.PSQLException \
  -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME"

# Check database logs
kubectl logs deployment/postgres -n exalt-shared
```

#### 2. Redis Connection Issues

```bash
# Test Redis connectivity
kubectl exec -it deployment/user-profile-service -- sh -c "
  redis-cli -h $REDIS_HOST -p $REDIS_PORT ping"

# Check Redis logs
kubectl logs deployment/redis -n exalt-shared
```

#### 3. Application Startup Issues

```bash
# Check application logs
kubectl logs deployment/user-profile-service -n exalt-shared

# Check configuration
kubectl describe configmap user-profile-config -n exalt-shared

# Verify secrets
kubectl get secrets user-profile-secrets -n exalt-shared -o yaml
```

### Performance Issues

#### Memory Issues

```bash
# Check memory usage
kubectl top pods -n exalt-shared

# Analyze heap dump
kubectl exec -it deployment/user-profile-service -- jcmd 1 GC.run_finalization
kubectl exec -it deployment/user-profile-service -- jcmd 1 VM.memory_usage
```

#### Database Performance

```sql
-- Check slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Check index usage
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE tablename = 'user_profile';
```

### Security Issues

#### SSL/TLS Configuration

```bash
# Test SSL endpoint
openssl s_client -connect user-profile-service:8095 -servername exalt.com

# Check certificate validity
curl -vI https://api.exalt.com/user-profile-service/health
```

#### OAuth2 Issues

```bash
# Test OAuth2 endpoints
curl -X POST https://api.exalt.com/user-profile-service/oauth2/authorization/google

# Check OAuth2 configuration
kubectl get configmap user-profile-config -o yaml | grep -A 20 oauth2
```

## Backup and Recovery

### Database Backup

```bash
# Create backup script
cat > scripts/backup-database.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backups/user-profile-service"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/user_profile_backup_$DATE.sql"

mkdir -p $BACKUP_DIR

pg_dump -h $DB_HOST -U $DB_USERNAME -d $DB_NAME > $BACKUP_FILE
gzip $BACKUP_FILE

# Keep only last 30 days of backups
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
EOF

chmod +x scripts/backup-database.sh
```

### Application Data Backup

```bash
# Backup user avatars
aws s3 sync s3://exalt-user-profiles-prod/avatars /backups/avatars/$(date +%Y%m%d)

# Backup Redis data (if needed)
kubectl exec deployment/redis -- redis-cli BGSAVE
```

---

**Document Version**: 1.0.0  
**Last Updated**: 2024-06-25  
**Document Owner**: Exalt Application Limited - DevOps Team  
**Review Cycle**: Monthly