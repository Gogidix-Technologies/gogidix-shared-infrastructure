# Setup Guide - Analytics Engine

## Overview

This document provides comprehensive setup instructions for the Analytics Engine service, including local development environment, Docker setup, and cloud deployment with big data components.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Setup](#kubernetes-setup)
5. [Configuration](#configuration)
6. [Data Pipeline Setup](#data-pipeline-setup)
7. [Machine Learning Setup](#machine-learning-setup)
8. [Verification](#verification)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 16GB RAM (32GB recommended for ML workloads)
- **Storage**: Minimum 50GB free space (100GB+ for data processing)
- **Network**: Stable high-speed internet connection

### Required Software

#### Development Tools

```bash
# Java Development Kit
Java 17+ (OpenJDK or Oracle JDK)

# Python for ML components
Python 3.9+ with pip

# Build Tools
Maven 3.8+
npm 8+ (for dashboard components)

# IDE (Recommended)
IntelliJ IDEA or PyCharm

# Version Control
Git 2.30+
```

#### Big Data and Analytics Stack

```bash
# Apache Kafka (Message Streaming)
Apache Kafka 3.0+

# Apache Spark (Data Processing)
Apache Spark 3.3+

# ClickHouse (Analytics Database)
ClickHouse 22.8+

# InfluxDB (Time Series Database)
InfluxDB 2.0+

# Elasticsearch (Search and Analytics)
Elasticsearch 8.0+
```

#### Container and Orchestration

```bash
# Docker
Docker Desktop 4.0+ or Docker Engine 20.10+
Docker Compose 2.0+

# Kubernetes (for cloud deployment)
kubectl 1.24+
helm 3.8+

# Monitoring
Prometheus 2.37+
Grafana 9.0+
```

### Installation Instructions

#### Java 17 and Maven

**Windows:**
```powershell
# Using Chocolatey
choco install openjdk17 maven

# Or download from official websites
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@17 maven

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk maven

# CentOS/RHEL
sudo yum install java-17-openjdk-devel maven
```

#### Python and ML Libraries

```bash
# Install Python 3.9+
python3 --version

# Install required ML libraries
pip install pandas numpy scikit-learn tensorflow torch
pip install apache-airflow apache-beam pyspark
pip install great-expectations mlflow feast
```

#### Apache Spark

**Download and Setup:**
```bash
# Download Spark
wget https://archive.apache.org/dist/spark/spark-3.3.2/spark-3.3.2-bin-hadoop3.tgz
tar -xzf spark-3.3.2-bin-hadoop3.tgz
sudo mv spark-3.3.2-bin-hadoop3 /opt/spark

# Add to PATH
echo 'export SPARK_HOME=/opt/spark' >> ~/.bashrc
echo 'export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin' >> ~/.bashrc
source ~/.bashrc
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/analytics-engine
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
SERVER_PORT=8200
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
CLICKHOUSE_HOST=localhost
CLICKHOUSE_PORT=8123
CLICKHOUSE_DB=analytics_db
CLICKHOUSE_USER=analytics_user
CLICKHOUSE_PASSWORD=secure_password

# InfluxDB Configuration
INFLUXDB_URL=http://localhost:8086
INFLUXDB_TOKEN=your-influxdb-token
INFLUXDB_ORG=exalt
INFLUXDB_BUCKET=analytics

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ANALYTICS_TOPIC=analytics-events
KAFKA_CONSUMER_GROUP=analytics-consumer-group

# Elasticsearch Configuration
ELASTICSEARCH_HOST=localhost
ELASTICSEARCH_PORT=9200
ELASTICSEARCH_USERNAME=elastic
ELASTICSEARCH_PASSWORD=secure_password

# Spark Configuration
SPARK_MASTER_URL=local[*]
SPARK_APP_NAME=analytics-engine

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# Security
JWT_SECRET=your-analytics-secret-key
API_KEY_SECRET=your-api-key-secret

# ML Configuration
MLFLOW_TRACKING_URI=http://localhost:5000
FEATURE_STORE_URI=localhost:6566

# Monitoring
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
```

### 3. Infrastructure Setup

#### Option A: Docker Compose (Recommended)

```bash
# Start analytics infrastructure
docker-compose -f docker-compose.analytics.yml up -d

# Services included:
# - ClickHouse (analytics database)
# - InfluxDB (time series database)
# - Kafka + Zookeeper (event streaming)
# - Elasticsearch (search and analytics)
# - MLflow (ML experiment tracking)
# - Prometheus + Grafana (monitoring)

# Verify services are running
docker-compose -f docker-compose.analytics.yml ps
```

#### Option B: Native Installation

**ClickHouse:**
```bash
# Install ClickHouse
curl https://clickhouse.com/ | sh
./clickhouse install

# Start ClickHouse
sudo service clickhouse-server start

# Create database and user
clickhouse-client --query "CREATE DATABASE analytics_db"
clickhouse-client --query "CREATE USER analytics_user IDENTIFIED BY 'secure_password'"
clickhouse-client --query "GRANT ALL ON analytics_db.* TO analytics_user"
```

**Kafka:**
```bash
# Download and start Kafka
wget https://downloads.apache.org/kafka/2.8.1/kafka_2.13-2.8.1.tgz
tar -xzf kafka_2.13-2.8.1.tgz
cd kafka_2.13-2.8.1

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 4. Build and Run

```bash
# Install dependencies
mvn clean install

# Install Python dependencies
pip install -r requirements.txt

# Run database migrations
mvn flyway:migrate

# Start the analytics engine
mvn spring-boot:run
```

### 5. Initialize Analytics Components

```bash
# Initialize data pipelines
./scripts/init-pipelines.sh

# Setup ML models
./scripts/setup-ml-models.sh

# Create sample dashboards
curl -X POST http://localhost:8200/api/v1/analytics/initialize \
  -H "Content-Type: application/json" \
  -d '{"createSampleDashboards": true, "loadTestData": true}'
```

## Docker Setup

### 1. Build Docker Images

```bash
# Build analytics engine image
docker build -t analytics-engine:latest .

# Build ML components image
docker build -f Dockerfile.ml -t analytics-engine-ml:latest .

# Or use the build script
./scripts/build-docker.sh
```

### 2. Run with Docker Compose

```bash
# Start all analytics services
docker-compose -f docker-compose.full.yml up -d

# Check logs
docker-compose logs -f analytics-engine

# Scale analytics workers
docker-compose up -d --scale analytics-worker=3
```

### 3. Docker Configuration

```yaml
# docker-compose.analytics.yml
version: '3.8'
services:
  analytics-engine:
    image: analytics-engine:latest
    ports:
      - "8200:8200"
      - "9090:9090"  # Prometheus metrics
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - CLICKHOUSE_HOST=clickhouse
      - INFLUXDB_URL=http://influxdb:8086
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - ELASTICSEARCH_HOST=elasticsearch
    depends_on:
      - clickhouse
      - influxdb
      - kafka
      - elasticsearch
    
  clickhouse:
    image: clickhouse/clickhouse-server:22.8
    ports:
      - "8123:8123"
      - "9000:9000"
    environment:
      CLICKHOUSE_DB: analytics_db
      CLICKHOUSE_USER: analytics_user
      CLICKHOUSE_PASSWORD: secure_password
    
  influxdb:
    image: influxdb:2.0
    ports:
      - "8086:8086"
    environment:
      INFLUXDB_DB: analytics
      INFLUXDB_USER: admin
      INFLUXDB_PASSWORD: secure_password
    
  kafka:
    image: confluentinc/cp-kafka:7.0.1
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    depends_on:
      - zookeeper
```

## Kubernetes Setup

### 1. Prerequisites

```bash
# Create namespace
kubectl create namespace analytics

# Create secrets for databases
kubectl create secret generic analytics-secrets \
  --from-literal=clickhouse-password=secure_password \
  --from-literal=influxdb-token=your-influxdb-token \
  --from-literal=elasticsearch-password=secure_password \
  --from-literal=jwt-secret=your-jwt-secret \
  -n analytics

# Create configmap for Spark configuration
kubectl create configmap spark-config \
  --from-file=spark-defaults.conf \
  --from-file=log4j.properties \
  -n analytics
```

### 2. Deploy with Kubernetes

```bash
# Apply all configurations
kubectl apply -f k8s/ -n analytics

# Check deployment status
kubectl get pods -l app=analytics-engine -n analytics

# View logs
kubectl logs -f deployment/analytics-engine -n analytics

# Check services
kubectl get services -n analytics
```

### 3. Helm Deployment

```bash
# Add Helm repositories
helm repo add exalt https://charts.exalt.com
helm repo add confluent https://confluentinc.github.io/cp-helm-charts/
helm repo add elastic https://helm.elastic.co

# Install the analytics stack
helm install analytics-stack exalt/analytics-engine \
  --namespace analytics \
  --set image.tag=latest \
  --set kafka.enabled=true \
  --set elasticsearch.enabled=true \
  --set spark.enabled=true

# Install monitoring stack
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace
```

## Configuration

### Application Configuration

```yaml
# application.yml
spring:
  application:
    name: analytics-engine
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: analytics-consumer-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: ${SERVER_PORT:8200}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}

analytics:
  clickhouse:
    url: jdbc:clickhouse://${CLICKHOUSE_HOST:localhost}:${CLICKHOUSE_PORT:8123}/${CLICKHOUSE_DB:analytics_db}
    username: ${CLICKHOUSE_USER:analytics_user}
    password: ${CLICKHOUSE_PASSWORD:secure_password}
  
  influxdb:
    url: ${INFLUXDB_URL:http://localhost:8086}
    token: ${INFLUXDB_TOKEN:your-token}
    org: ${INFLUXDB_ORG:exalt}
    bucket: ${INFLUXDB_BUCKET:analytics}
  
  spark:
    master: ${SPARK_MASTER_URL:local[*]}
    app-name: ${SPARK_APP_NAME:analytics-engine}
    
  ml:
    mlflow-uri: ${MLFLOW_TRACKING_URI:http://localhost:5000}
    feature-store-uri: ${FEATURE_STORE_URI:localhost:6566}
```

### Analytics Components Configuration

```yaml
# analytics-config.yml
analytics:
  pipelines:
    batch-processing:
      schedule: "0 */1 * * * *"  # Every hour
      parallelism: 4
    stream-processing:
      window-size: 60s
      watermark: 10s
    
  dashboards:
    refresh-interval: 30s
    cache-ttl: 300s
    max-widgets: 50
    
  ml:
    training:
      max-iterations: 1000
      batch-size: 1000
    serving:
      timeout: 5s
      max-concurrent: 100
      
  storage:
    data-retention:
      raw-events: 90d
      aggregated-metrics: 1y
      ml-models: 2y
```

## Data Pipeline Setup

### 1. Create Data Pipelines

```bash
# Setup Airflow for pipeline orchestration
pip install apache-airflow[postgres,kafka,spark]

# Initialize Airflow database
airflow db init

# Create analytics pipelines
./scripts/create-analytics-pipelines.sh

# Start Airflow services
airflow webserver --port 8080 &
airflow scheduler &
```

### 2. Configure Data Sources

```bash
# Test database connections
./scripts/test-data-connections.sh

# Setup data source configurations
curl -X POST http://localhost:8200/api/v1/analytics/datasources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "social-commerce-db",
    "type": "postgresql",
    "connectionString": "jdbc:postgresql://localhost:5432/social_commerce",
    "username": "app_user",
    "password": "secure_password"
  }'
```

## Machine Learning Setup

### 1. Setup MLflow

```bash
# Start MLflow tracking server
mlflow server \
  --backend-store-uri postgresql://mlflow_user:password@localhost:5432/mlflow \
  --default-artifact-root s3://mlflow-artifacts \
  --host 0.0.0.0 \
  --port 5000
```

### 2. Initialize ML Models

```bash
# Setup feature store
./scripts/setup-feature-store.sh

# Train initial models
python scripts/train_initial_models.py

# Deploy models for serving
./scripts/deploy-ml-models.sh
```

## Verification

### 1. Health Checks

```bash
# Check service health
curl http://localhost:8200/actuator/health

# Check analytics components
curl http://localhost:8200/api/v1/analytics/components/status

# Verify data pipelines
curl http://localhost:8200/api/v1/analytics/pipelines/status

# Check ML models
curl http://localhost:8200/api/v1/analytics/ml/models/status
```

### 2. Analytics Testing

```bash
# Test real-time analytics
curl -X POST http://localhost:8200/api/v1/analytics/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "user_action",
    "userId": "test-user-123",
    "action": "product_view",
    "productId": "prod-456",
    "timestamp": "2024-06-24T10:30:00Z"
  }'

# Test dashboard creation
curl -X POST http://localhost:8200/api/v1/analytics/dashboards \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Dashboard",
    "widgets": [
      {
        "type": "kpi",
        "title": "Total Events",
        "query": "SELECT count() FROM events"
      }
    ]
  }'
```

### 3. ML Model Testing

```bash
# Test customer segmentation
curl -X POST http://localhost:8200/api/v1/analytics/ml/segment \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123"
  }'

# Test churn prediction
curl -X POST http://localhost:8200/api/v1/analytics/ml/predict/churn \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "features": {
      "lastPurchaseDays": 30,
      "totalSpent": 500.0,
      "orderCount": 10
    }
  }'
```

## Troubleshooting

### Common Issues

#### 1. Kafka Connection Issues

**Problem**: Cannot connect to Kafka brokers

**Solutions:**
```bash
# Check Kafka status
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Test connectivity
telnet localhost 9092

# Check Kafka logs
docker logs kafka-container-name
```

#### 2. ClickHouse Query Performance

**Problem**: Slow query performance

**Solutions:**
```bash
# Check query execution
clickhouse-client --query "EXPLAIN SELECT * FROM events WHERE date >= today()"

# Add indexes
clickhouse-client --query "ALTER TABLE events ADD INDEX idx_date date TYPE minmax GRANULARITY 1"

# Optimize table
clickhouse-client --query "OPTIMIZE TABLE events"
```

#### 3. Spark Job Failures

**Problem**: Spark jobs failing with memory errors

**Solutions:**
```bash
# Increase driver memory
export SPARK_DRIVER_MEMORY=4g

# Increase executor memory
export SPARK_EXECUTOR_MEMORY=8g

# Adjust parallelism
spark-submit --conf spark.sql.adaptive.coalescePartitions.enabled=true
```

#### 4. ML Model Training Issues

**Problem**: Model training failing or taking too long

**Solutions:**
```bash
# Check feature data quality
python scripts/validate_features.py

# Reduce training data size for testing
python scripts/train_with_sample.py --sample-ratio 0.1

# Monitor training progress
mlflow ui --host 0.0.0.0 --port 5000
```

### Performance Tuning

```bash
# JVM tuning for analytics workloads
export JAVA_OPTS="
  -Xmx8g 
  -Xms8g 
  -XX:+UseG1GC 
  -XX:MaxGCPauseMillis=200 
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
"

# ClickHouse optimization
clickhouse-client --query "
  SET max_memory_usage = 20000000000;
  SET max_bytes_before_external_group_by = 20000000000;
  SET max_bytes_before_external_sort = 20000000000;
"
```

## Next Steps

1. **Configure Data Sources**: Add your business data sources
2. **Create Custom Dashboards**: Build domain-specific analytics dashboards
3. **Setup Alerts**: Configure business metric alerts
4. **Train Custom Models**: Develop business-specific ML models
5. **Setup Monitoring**: Configure comprehensive monitoring and alerting
6. **Performance Optimization**: Tune for your specific workload patterns

## Support

- **Documentation**: `/docs`
- **API Reference**: `http://localhost:8200/swagger-ui.html`
- **ML Models**: `http://localhost:5000` (MLflow UI)
- **Monitoring**: `http://localhost:3000` (Grafana)
- **Issues**: GitHub Issues
- **Team Chat**: Slack #analytics-engine
- **Email**: analytics-team@exalt.com