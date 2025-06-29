# Message Broker Operations Guide

## Overview

This document provides comprehensive operational procedures for managing the Message Broker Service (Apache Kafka) in production environments, including monitoring, maintenance, incident response, and performance optimization.

## Table of Contents

1. [Service Operations](#service-operations)
2. [Monitoring and Alerting](#monitoring-and-alerting)
3. [Performance Management](#performance-management)
4. [Incident Response](#incident-response)
5. [Maintenance Procedures](#maintenance-procedures)
6. [Backup and Recovery](#backup-and-recovery)
7. [Security Operations](#security-operations)
8. [Capacity Planning](#capacity-planning)
9. [Troubleshooting Guide](#troubleshooting-guide)

## Service Operations

### Daily Operations Checklist

#### Morning Operations (8:00 AM)
- [ ] Check cluster health status
- [ ] Review overnight alerts and incidents
- [ ] Verify all brokers are online
- [ ] Check partition leader distribution
- [ ] Monitor disk usage across brokers
- [ ] Review consumer lag metrics
- [ ] Validate replication health
- [ ] Check under-replicated partitions

#### Mid-Day Operations (1:00 PM)
- [ ] Monitor peak traffic performance
- [ ] Review producer/consumer metrics
- [ ] Check resource utilization
- [ ] Validate backup completion
- [ ] Review security alerts
- [ ] Check topic configurations

#### Evening Operations (6:00 PM)
- [ ] Review daily performance summary
- [ ] Check log retention and cleanup
- [ ] Plan maintenance activities
- [ ] Update operational documentation
- [ ] Prepare next day priorities

### Health Check Commands

```bash
# Cluster Health Overview
kafka-topics --bootstrap-server localhost:9092 \
  --describe --under-replicated-partitions

kafka-topics --bootstrap-server localhost:9092 \
  --describe --unavailable-partitions

# Broker Health
kafka-broker-api-versions --bootstrap-server localhost:9092

# Consumer Group Health
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --all-groups

# Log Directory Health
kafka-log-dirs --describe --bootstrap-server localhost:9092 \
  --json | jq '.brokers[].logDirs[] | select(.error != null)'
```

### Service Level Objectives

| Metric | Target | Warning | Critical |
|--------|--------|---------|----------|
| Cluster Availability | 99.99% | 99.9% | < 99.9% |
| Message Latency (p99) | < 100ms | > 200ms | > 500ms |
| Consumer Lag | < 1000 msgs | > 10k msgs | > 100k msgs |
| Disk Usage | < 80% | > 85% | > 90% |
| Under-replicated Partitions | 0 | > 0 for 5m | > 0 for 15m |

## Monitoring and Alerting

### Critical Metrics

#### Cluster Health Metrics
```yaml
metrics:
  cluster:
    - kafka_controller_kafkacontroller_activecontrollercount
    - kafka_server_replicamanager_underreplicatedpartitions
    - kafka_server_replicamanager_offlinereplicacount
    - kafka_cluster_partition_underminisr
    - kafka_cluster_partition_atminisrpartitioncount

  broker:
    - kafka_server_kafkarequesthandlerpool_requesthandleravgidlepercent
    - kafka_server_brokertopicmetrics_messagesinpersec
    - kafka_server_brokertopicmetrics_bytesinpersec
    - kafka_server_brokertopicmetrics_bytesoutpersec
    - kafka_network_requestmetrics_totaltimems

  consumer:
    - kafka_consumer_lag_sum
    - kafka_consumer_lag_max
    - kafka_consumer_records_consumed_rate
    - kafka_consumer_fetch_latency_avg
```

### Alert Rules

```yaml
groups:
- name: kafka.rules
  rules:
  
  # Critical Alerts
  - alert: KafkaBrokerDown
    expr: up{job="kafka"} == 0
    for: 2m
    labels:
      severity: critical
      team: platform
    annotations:
      summary: "Kafka broker is down"
      description: "Kafka broker {{ $labels.instance }} is down for more than 2 minutes"
      runbook: "https://wiki.example.com/runbooks/kafka-broker-down"

  - alert: KafkaUnderReplicatedPartitions
    expr: kafka_server_replicamanager_underreplicatedpartitions > 0
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Kafka has under-replicated partitions"
      description: "{{ $value }} partitions are under-replicated on {{ $labels.instance }}"

  - alert: KafkaOfflinePartitions
    expr: kafka_controller_kafkacontroller_offlinepartitionscount > 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Kafka has offline partitions"
      description: "{{ $value }} partitions are offline"

  - alert: KafkaConsumerLag
    expr: kafka_consumer_lag_sum > 100000
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High consumer lag detected"
      description: "Consumer group {{ $labels.group }} has lag of {{ $value }} messages"

  - alert: KafkaDiskSpaceHigh
    expr: (kafka_log_size / kafka_log_dir_size) > 0.9
    for: 15m
    labels:
      severity: critical
    annotations:
      summary: "Kafka disk space is running low"
      description: "Disk usage on {{ $labels.instance }} is above 90%"

  # Warning Alerts
  - alert: KafkaHighThroughput
    expr: rate(kafka_server_brokertopicmetrics_messagesinpersec[5m]) > 10000
    for: 15m
    labels:
      severity: warning
    annotations:
      summary: "High message throughput detected"

  - alert: KafkaSlowProducer
    expr: kafka_producer_request_latency_avg > 500
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "Producer latency is high"
```

### Monitoring Dashboards

#### Grafana Dashboard Configuration
```json
{
  "dashboard": {
    "title": "Kafka Operations Dashboard",
    "panels": [
      {
        "title": "Cluster Overview",
        "type": "stat",
        "targets": [
          {
            "expr": "kafka_server_kafkaserver_brokerstate",
            "legendFormat": "Broker State"
          },
          {
            "expr": "kafka_controller_kafkacontroller_activecontrollercount", 
            "legendFormat": "Active Controller"
          }
        ]
      },
      {
        "title": "Message Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(kafka_server_brokertopicmetrics_messagesinpersec[5m]))",
            "legendFormat": "Messages In/sec"
          },
          {
            "expr": "sum(rate(kafka_server_brokertopicmetrics_messagesoutpersec[5m]))",
            "legendFormat": "Messages Out/sec"
          }
        ]
      },
      {
        "title": "Partition Health",
        "type": "graph", 
        "targets": [
          {
            "expr": "kafka_server_replicamanager_underreplicatedpartitions",
            "legendFormat": "Under Replicated"
          },
          {
            "expr": "kafka_controller_kafkacontroller_offlinepartitionscount",
            "legendFormat": "Offline Partitions"
          }
        ]
      },
      {
        "title": "Consumer Lag",
        "type": "graph",
        "targets": [
          {
            "expr": "kafka_consumer_lag_sum by (group)",
            "legendFormat": "{{ group }}"
          }
        ]
      },
      {
        "title": "Request Latency",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.99, kafka_network_requestmetrics_totaltimems)",
            "legendFormat": "99th percentile"
          },
          {
            "expr": "histogram_quantile(0.95, kafka_network_requestmetrics_totaltimems)",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "Disk Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "(kafka_log_size / kafka_log_dir_size) * 100",
            "legendFormat": "{{ instance }}"
          }
        ]
      }
    ]
  }
}
```

## Performance Management

### Performance Baselines

| Component | Metric | Baseline | Target |
|-----------|--------|----------|---------|
| Producer | Throughput | 100k msgs/sec | 500k msgs/sec |
| Consumer | Throughput | 150k msgs/sec | 750k msgs/sec |
| End-to-end | Latency (p99) | < 100ms | < 50ms |
| Replication | Lag | < 10ms | < 5ms |
| Disk I/O | Utilization | < 70% | < 50% |

### Performance Optimization

#### Producer Optimization
```bash
# Optimal producer configuration
kafka-console-producer --bootstrap-server localhost:9092 \
  --topic test-topic \
  --producer-property batch.size=32768 \
  --producer-property linger.ms=20 \
  --producer-property compression.type=lz4 \
  --producer-property acks=1 \
  --producer-property max.in.flight.requests.per.connection=5

# Benchmark producer performance
kafka-producer-perf-test --topic test-perf \
  --num-records 1000000 \
  --record-size 1024 \
  --throughput 100000 \
  --producer-props bootstrap.servers=localhost:9092 \
    batch.size=32768 \
    linger.ms=20 \
    compression.type=lz4
```

#### Consumer Optimization
```bash
# Optimal consumer configuration
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic test-topic \
  --consumer-property fetch.min.bytes=1024 \
  --consumer-property fetch.max.wait.ms=500 \
  --consumer-property max.poll.records=500

# Benchmark consumer performance
kafka-consumer-perf-test --bootstrap-server localhost:9092 \
  --topic test-perf \
  --messages 1000000 \
  --consumer.config consumer-perf.properties
```

#### Broker Optimization
```bash
# Check and optimize JVM settings
export KAFKA_HEAP_OPTS="-Xms8g -Xmx8g"
export KAFKA_JVM_PERFORMANCE_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35"

# Monitor GC performance
jstat -gc $(pgrep -f kafka.Kafka) 5s

# Optimize log cleaner
kafka-configs --bootstrap-server localhost:9092 \
  --entity-type brokers \
  --entity-name 1 \
  --alter \
  --add-config log.cleaner.threads=2,log.cleaner.io.max.bytes.per.second=5242880
```

### Load Testing

```bash
# Stress test script
#!/bin/bash
# stress-test.sh

echo "Starting Kafka stress test..."

# Create test topic
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic stress-test \
  --partitions 12 \
  --replication-factor 3

# Producer stress test
kafka-producer-perf-test \
  --topic stress-test \
  --num-records 5000000 \
  --record-size 1024 \
  --throughput 50000 \
  --producer-props bootstrap.servers=localhost:9092 \
    batch.size=32768 \
    linger.ms=20 \
    compression.type=lz4 \
    acks=1 &

# Consumer stress test
kafka-consumer-perf-test \
  --bootstrap-server localhost:9092 \
  --topic stress-test \
  --messages 5000000 \
  --consumer.config stress-consumer.properties &

# Monitor during test
while true; do
  echo "=== $(date) ==="
  kafka-consumer-groups --bootstrap-server localhost:9092 \
    --describe --group perf-consumer
  sleep 30
done
```

## Incident Response

### Incident Classification

| Severity | Response Time | Examples |
|----------|---------------|----------|
| P1 | 15 minutes | Cluster down, data loss |
| P2 | 30 minutes | Broker failures, high lag |
| P3 | 2 hours | Performance degradation |
| P4 | 24 hours | Minor issues, warnings |

### Response Procedures

#### P1 - Cluster Failure
```bash
#!/bin/bash
# cluster-failure-response.sh

echo "P1 INCIDENT: Cluster failure detected"

# 1. Check cluster state
kafka-metadata-shell --snapshot /var/kafka/logs/__cluster_metadata-0/00000000000000000000.log --print-brokers

# 2. Check Zookeeper
echo "stat" | nc localhost 2181

# 3. Check broker logs
tail -n 100 /var/kafka/logs/server.log

# 4. Attempt broker restart
sudo systemctl restart kafka

# 5. Force leader election if needed
kafka-leader-election --bootstrap-server localhost:9092 \
  --election-type preferred \
  --all-topic-partitions

# 6. Verify cluster recovery
kafka-topics --bootstrap-server localhost:9092 \
  --describe --under-replicated-partitions
```

#### P2 - High Consumer Lag
```bash
#!/bin/bash
# high-lag-response.sh

GROUP_ID=$1
TOPIC=$2

echo "P2 INCIDENT: High consumer lag for group $GROUP_ID"

# 1. Check consumer group status
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group $GROUP_ID

# 2. Check if consumers are active
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group $GROUP_ID --members

# 3. Scale consumer group if needed
kubectl scale deployment ${GROUP_ID}-consumer --replicas=6

# 4. Consider increasing partitions
CURRENT_PARTITIONS=$(kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic $TOPIC | grep PartitionCount | awk '{print $4}')
  
if [ $CURRENT_PARTITIONS -lt 12 ]; then
  kafka-topics --alter \
    --bootstrap-server localhost:9092 \
    --topic $TOPIC \
    --partitions 12
fi

# 5. Monitor recovery
watch -n 10 "kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group $GROUP_ID"
```

### Emergency Procedures

#### Split Brain Recovery
```bash
# Identify the situation
kafka-metadata-shell --snapshot /var/kafka/logs/__cluster_metadata-0/00000000000000000000.log --print-brokers

# Stop all brokers
sudo systemctl stop kafka

# Clean up problematic metadata
rm -rf /var/kafka/logs/__cluster_metadata-*

# Start brokers one by one
sudo systemctl start kafka@1
sleep 30
sudo systemctl start kafka@2
sleep 30
sudo systemctl start kafka@3
```

#### Data Recovery
```bash
# Restore from backup if data loss detected
kafka-topics --delete --bootstrap-server localhost:9092 --topic corrupted-topic
kafka-topics --create --bootstrap-server localhost:9092 \
  --topic corrupted-topic \
  --partitions 6 \
  --replication-factor 3

# Restore from backup tool
kafka-mirror-maker --consumer.config consumer.properties \
  --producer.config producer.properties \
  --whitelist "corrupted-topic"
```

## Maintenance Procedures

### Routine Maintenance

#### Daily Tasks
```bash
#!/bin/bash
# daily-maintenance.sh

echo "Starting daily Kafka maintenance - $(date)"

# 1. Health check
./health-check.sh

# 2. Log cleanup
find /var/kafka/logs -name "*.log" -mtime +7 -delete
find /var/kafka/logs -name "*.index" -mtime +7 -delete

# 3. Metrics collection
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --all-groups > /var/log/kafka/consumer-groups-$(date +%Y%m%d).log

# 4. Backup configurations
kafka-topics --bootstrap-server localhost:9092 \
  --describe > /var/backups/kafka/topics-$(date +%Y%m%d).backup

# 5. Check disk usage
df -h /var/kafka/logs

echo "Daily maintenance completed - $(date)"
```

#### Weekly Tasks
```bash
#!/bin/bash
# weekly-maintenance.sh

echo "Starting weekly Kafka maintenance - $(date)"

# 1. Leader rebalancing
kafka-leader-election --bootstrap-server localhost:9092 \
  --election-type preferred \
  --all-topic-partitions

# 2. Log segment optimization
for topic in $(kafka-topics --list --bootstrap-server localhost:9092); do
  kafka-configs --bootstrap-server localhost:9092 \
    --entity-type topics \
    --entity-name $topic \
    --alter \
    --add-config segment.ms=604800000  # 7 days
done

# 3. Update topic configurations if needed
kafka-configs --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name high-volume-topic \
  --alter \
  --add-config retention.ms=2592000000  # 30 days

echo "Weekly maintenance completed - $(date)"
```

### Rolling Updates

#### Broker Rolling Update
```bash
#!/bin/bash
# rolling-update.sh

BROKERS=(kafka-1 kafka-2 kafka-3)

for broker in "${BROKERS[@]}"; do
  echo "Updating $broker..."
  
  # 1. Gracefully shutdown
  ssh $broker "sudo systemctl stop kafka"
  
  # 2. Wait for leadership migration
  sleep 60
  
  # 3. Update and restart
  ssh $broker "sudo yum update kafka -y"
  ssh $broker "sudo systemctl start kafka"
  
  # 4. Wait for broker to rejoin
  while ! kafka-broker-api-versions --bootstrap-server $broker:9092 > /dev/null 2>&1; do
    echo "Waiting for $broker to come online..."
    sleep 10
  done
  
  # 5. Verify partition health
  UNDER_REPLICATED=$(kafka-topics --bootstrap-server $broker:9092 \
    --describe --under-replicated-partitions | wc -l)
  
  if [ $UNDER_REPLICATED -gt 0 ]; then
    echo "WARNING: Under-replicated partitions detected"
    exit 1
  fi
  
  echo "$broker updated successfully"
  sleep 30
done
```

## Backup and Recovery

### Backup Strategy

```yaml
backup_strategy:
  metadata:
    schedule: "0 */6 * * *"  # Every 6 hours
    retention: 30_days
    includes:
      - topic_configurations
      - consumer_group_offsets
      - acl_configurations
      
  data:
    schedule: "0 2 * * *"    # Daily at 2 AM
    retention: 7_days
    method: kafka_mirror_maker
    
  snapshots:
    schedule: "0 0 * * 0"    # Weekly
    retention: 4_weeks
    method: filesystem_snapshot
```

### Backup Procedures

```bash
#!/bin/bash
# backup-kafka.sh

BACKUP_DIR="/var/backups/kafka/$(date +%Y%m%d)"
mkdir -p $BACKUP_DIR

echo "Starting Kafka backup - $(date)"

# 1. Backup topic metadata
kafka-topics --bootstrap-server localhost:9092 \
  --describe > $BACKUP_DIR/topics.backup

# 2. Backup consumer group offsets
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --all-groups --describe > $BACKUP_DIR/consumer-groups.backup

# 3. Backup ACLs
kafka-acls --bootstrap-server localhost:9092 \
  --list > $BACKUP_DIR/acls.backup

# 4. Backup configurations
kafka-configs --bootstrap-server localhost:9092 \
  --entity-type topics --describe > $BACKUP_DIR/topic-configs.backup

kafka-configs --bootstrap-server localhost:9092 \
  --entity-type brokers --describe > $BACKUP_DIR/broker-configs.backup

# 5. Create data backup using MirrorMaker
nohup kafka-mirror-maker \
  --consumer.config backup-consumer.properties \
  --producer.config backup-producer.properties \
  --whitelist ".*" > $BACKUP_DIR/mirror-maker.log 2>&1 &

echo "Backup initiated - $(date)"
```

### Recovery Procedures

```bash
#!/bin/bash
# restore-kafka.sh

BACKUP_DATE=$1
BACKUP_DIR="/var/backups/kafka/$BACKUP_DATE"

if [ ! -d "$BACKUP_DIR" ]; then
  echo "Backup directory $BACKUP_DIR not found"
  exit 1
fi

echo "Starting Kafka restore from $BACKUP_DATE"

# 1. Restore topics
while IFS= read -r line; do
  if [[ $line == Topic:* ]]; then
    TOPIC=$(echo $line | awk '{print $2}')
    PARTITIONS=$(echo $line | awk '{print $4}')
    REPLICATION=$(echo $line | awk '{print $6}')
    
    kafka-topics --create \
      --bootstrap-server localhost:9092 \
      --topic $TOPIC \
      --partitions $PARTITIONS \
      --replication-factor $REPLICATION
  fi
done < $BACKUP_DIR/topics.backup

# 2. Restore ACLs
while IFS= read -r line; do
  if [[ $line == Current* ]]; then
    continue
  fi
  eval "kafka-acls --bootstrap-server localhost:9092 --add $line"
done < $BACKUP_DIR/acls.backup

# 3. Restore data using MirrorMaker
kafka-mirror-maker \
  --consumer.config restore-consumer.properties \
  --producer.config restore-producer.properties \
  --whitelist ".*"

echo "Restore completed - $(date)"
```

## Security Operations

### Security Monitoring

```bash
#!/bin/bash
# security-audit.sh

echo "Starting Kafka security audit - $(date)"

# 1. Check SSL certificates
for broker in kafka-1 kafka-2 kafka-3; do
  echo "Checking SSL certificate for $broker..."
  openssl s_client -connect $broker:9093 -servername $broker < /dev/null | \
    openssl x509 -noout -dates
done

# 2. Audit user access
kafka-acls --bootstrap-server localhost:9092 --list | \
  grep -v "Current ACLs" > /var/log/kafka/acl-audit-$(date +%Y%m%d).log

# 3. Check authentication logs
grep "AUTHENTICATION" /var/kafka/logs/kafka-authorizer.log | \
  tail -1000 > /var/log/kafka/auth-audit-$(date +%Y%m%d).log

# 4. Validate configurations
kafka-configs --bootstrap-server localhost:9092 \
  --entity-type brokers --describe | \
  grep -E "(ssl|sasl|security)" > /var/log/kafka/security-config-$(date +%Y%m%d).log

echo "Security audit completed - $(date)"
```

### Certificate Management

```bash
#!/bin/bash
# certificate-renewal.sh

CERT_DIR="/var/kafka/ssl"
NEW_CERT_DIR="/tmp/kafka-certs-new"

echo "Starting certificate renewal - $(date)"

# 1. Generate new certificates
mkdir -p $NEW_CERT_DIR
cd $NEW_CERT_DIR

# Generate CA
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -batch

# Generate server certificates for each broker
for broker in kafka-1 kafka-2 kafka-3; do
  keytool -keystore $broker.server.keystore.jks \
    -alias localhost -validity 365 -genkey -keyalg RSA \
    -dname "CN=$broker,OU=Engineering,O=Exalt,L=City,S=State,C=US" \
    -storepass changeme -keypass changeme
    
  keytool -keystore $broker.server.keystore.jks \
    -alias localhost -certreq -file $broker-cert-file \
    -storepass changeme
    
  openssl x509 -req -CA ca-cert -CAkey ca-key \
    -in $broker-cert-file -out $broker-cert-signed \
    -days 365 -CAcreateserial
    
  keytool -keystore $broker.server.keystore.jks \
    -alias CARoot -import -file ca-cert -noprompt \
    -storepass changeme
    
  keytool -keystore $broker.server.keystore.jks \
    -alias localhost -import -file $broker-cert-signed \
    -noprompt -storepass changeme
done

# 2. Rolling certificate update
for broker in kafka-1 kafka-2 kafka-3; do
  echo "Updating certificate for $broker..."
  
  # Copy new certificate
  scp $NEW_CERT_DIR/$broker.* $broker:$CERT_DIR/
  
  # Restart broker
  ssh $broker "sudo systemctl restart kafka"
  
  # Wait and verify
  sleep 30
  openssl s_client -connect $broker:9093 < /dev/null | \
    openssl x509 -noout -dates
done

echo "Certificate renewal completed - $(date)"
```

## Capacity Planning

### Monitoring Growth Patterns

```python
#!/usr/bin/env python3
# capacity-analysis.py

import requests
import pandas as pd
from datetime import datetime, timedelta

def get_kafka_metrics():
    """Fetch Kafka metrics from Prometheus"""
    prometheus_url = "http://prometheus:9090"
    
    queries = {
        'message_rate': 'sum(rate(kafka_server_brokertopicmetrics_messagesinpersec[5m]))',
        'disk_usage': 'kafka_log_size',
        'partition_count': 'kafka_server_replicamanager_partitioncount',
        'consumer_lag': 'kafka_consumer_lag_sum'
    }
    
    metrics = {}
    for name, query in queries.items():
        response = requests.get(f"{prometheus_url}/api/v1/query", 
                              params={'query': query})
        metrics[name] = response.json()['data']['result']
    
    return metrics

def analyze_growth(metrics):
    """Analyze growth patterns and predict capacity needs"""
    # Calculate growth rates
    message_growth = calculate_growth_rate(metrics['message_rate'])
    disk_growth = calculate_growth_rate(metrics['disk_usage'])
    
    # Project future needs
    projections = {
        '30_days': project_capacity(30, message_growth, disk_growth),
        '60_days': project_capacity(60, message_growth, disk_growth),
        '90_days': project_capacity(90, message_growth, disk_growth)
    }
    
    return projections

def recommend_scaling(projections):
    """Provide scaling recommendations"""
    recommendations = []
    
    for period, projection in projections.items():
        if projection['disk_usage'] > 0.8:
            recommendations.append({
                'action': 'add_storage',
                'timeframe': period,
                'details': f"Add {projection['additional_storage_gb']}GB storage"
            })
        
        if projection['message_rate'] > 100000:
            recommendations.append({
                'action': 'add_brokers',
                'timeframe': period,
                'details': f"Add {projection['additional_brokers']} brokers"
            })
    
    return recommendations

if __name__ == "__main__":
    metrics = get_kafka_metrics()
    projections = analyze_growth(metrics)
    recommendations = recommend_scaling(projections)
    
    print("Capacity Planning Report")
    print("=" * 50)
    for rec in recommendations:
        print(f"{rec['timeframe']}: {rec['action']} - {rec['details']}")
```

### Scaling Triggers

```yaml
scaling_policies:
  horizontal_scaling:
    triggers:
      - metric: message_rate
        threshold: 80000
        duration: 15m
        action: add_broker
        
      - metric: disk_usage_percent
        threshold: 75
        duration: 30m
        action: add_storage
        
      - metric: consumer_lag
        threshold: 50000
        duration: 10m
        action: scale_consumers
        
  vertical_scaling:
    triggers:
      - metric: cpu_usage
        threshold: 80
        duration: 15m
        action: increase_cpu
        
      - metric: memory_usage
        threshold: 85
        duration: 15m
        action: increase_memory
```

## Troubleshooting Guide

### Common Issues and Solutions

#### Issue: Under-replicated Partitions
```bash
# Diagnosis
kafka-topics --bootstrap-server localhost:9092 \
  --describe --under-replicated-partitions

# Solution 1: Restart affected brokers
sudo systemctl restart kafka

# Solution 2: Force leader election
kafka-leader-election --bootstrap-server localhost:9092 \
  --election-type preferred --all-topic-partitions

# Solution 3: Manual partition reassignment
kafka-reassign-partitions --bootstrap-server localhost:9092 \
  --reassignment-json-file reassignment.json --execute
```

#### Issue: Consumer Lag Buildup
```bash
# Diagnosis
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group my-group

# Solution 1: Scale consumer group
kubectl scale deployment my-consumer --replicas=6

# Solution 2: Increase partitions
kafka-topics --alter --bootstrap-server localhost:9092 \
  --topic my-topic --partitions 12

# Solution 3: Reset offsets if needed
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group my-group --reset-offsets --topic my-topic \
  --to-latest --execute
```

#### Issue: Disk Space Full
```bash
# Diagnosis
df -h /var/kafka/logs
du -sh /var/kafka/logs/*

# Solution 1: Immediate cleanup
kafka-configs --bootstrap-server localhost:9092 \
  --entity-type topics --entity-name old-topic \
  --alter --add-config retention.ms=3600000  # 1 hour

# Solution 2: Delete old topics
kafka-topics --delete --bootstrap-server localhost:9092 \
  --topic unused-topic

# Solution 3: Add storage
lvcreate -L 500G -n kafka-data2 vg0
mkfs.ext4 /dev/vg0/kafka-data2
mount /dev/vg0/kafka-data2 /var/kafka/logs2
```

### Debug Tools and Commands

```bash
# Comprehensive cluster status
kafka-metadata-shell --snapshot /var/kafka/logs/__cluster_metadata-0/00000000000000000000.log

# Log analysis
kafka-dump-log --files /var/kafka/logs/my-topic-0/00000000000000000000.log \
  --print-data-log

# Consumer group coordination
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group my-group --members --verbose

# Broker JMX metrics
java -cp $KAFKA_HOME/libs/* kafka.tools.JmxTool \
  --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec \
  --jmx-url service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi

# Network connectivity test
kafka-broker-api-versions --bootstrap-server localhost:9092

# Performance profiling
kafka-producer-perf-test --topic test-perf \
  --num-records 100000 --record-size 1024 \
  --throughput 10000 --producer-props bootstrap.servers=localhost:9092
```

## Operational Best Practices

### Change Management
1. Always test configuration changes in staging first
2. Implement changes during maintenance windows
3. Have rollback procedures ready
4. Document all changes with timestamps

### Monitoring Best Practices
1. Set up comprehensive alerting for all critical metrics
2. Monitor trends, not just absolute values
3. Use multiple monitoring tools for redundancy
4. Regular review and tuning of alert thresholds

### Performance Management
1. Baseline performance metrics during normal operations
2. Regular load testing to validate capacity
3. Proactive scaling before reaching limits
4. Continuous optimization of configurations

### Security Best Practices
1. Regular certificate rotation
2. Audit access logs monthly
3. Principle of least privilege for ACLs
4. Regular security updates and patches

## Contact Information

### Escalation Matrix
- L1 Support: kafka-support@example.com
- L2 Engineering: platform-engineering@example.com
- L3 On-Call: +1-555-KAFKA-911
- Manager: platform-manager@example.com

### External Support
- Confluent Support: support@confluent.io
- Apache Kafka Community: kafka-users@kafka.apache.org

For detailed procedures and additional runbooks, refer to the internal operations wiki.