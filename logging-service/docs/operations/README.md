# Logging Service Operations Guide

## Overview

This document provides comprehensive operational procedures for managing the Logging Service in production environments, including monitoring, maintenance, incident response, and performance optimization for the ELK stack.

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

#### Morning (9:00 AM)
- [ ] Check cluster health status
- [ ] Review overnight alerts
- [ ] Verify all nodes are operational
- [ ] Check disk usage across all nodes
- [ ] Review indexing rates and latency
- [ ] Validate backup completion
- [ ] Check for failed shards

#### Afternoon (2:00 PM)
- [ ] Monitor query performance
- [ ] Review resource utilization
- [ ] Check Kafka lag
- [ ] Verify log ingestion rates
- [ ] Review error logs
- [ ] Check certificate expiration

#### Evening (6:00 PM)
- [ ] Review daily metrics
- [ ] Plan maintenance windows
- [ ] Update operational runbook
- [ ] Prepare next day priorities

### Health Monitoring

```bash
# Cluster Health Check
curl -X GET "localhost:9200/_cluster/health?pretty"

# Node Statistics
curl -X GET "localhost:9200/_nodes/stats?pretty"

# Index Health
curl -X GET "localhost:9200/_cat/indices?v&health=yellow&health=red"

# Shard Allocation
curl -X GET "localhost:9200/_cat/shards?v&h=index,shard,prirep,state,unassigned.reason"

# Pending Tasks
curl -X GET "localhost:9200/_cluster/pending_tasks?pretty"
```

### Service Availability

```yaml
# Availability Targets
- Elasticsearch Cluster: 99.95% uptime
- Kibana Interface: 99.9% uptime
- Log Ingestion: < 5 second latency
- Query Response: < 2 seconds for 95th percentile
- Data Loss: Zero tolerance
```

## Monitoring and Alerting

### Key Metrics

#### Elasticsearch Metrics
```prometheus
# Cluster Health
elasticsearch_cluster_health_status{cluster="logs-cluster"}

# Node Availability
elasticsearch_cluster_health_number_of_nodes
elasticsearch_cluster_health_number_of_data_nodes

# Indexing Performance
elasticsearch_indices_indexing_index_total
elasticsearch_indices_indexing_index_time_seconds_total
elasticsearch_indices_indexing_throttle_time_seconds_total

# Search Performance
elasticsearch_indices_search_query_total
elasticsearch_indices_search_query_time_seconds_total
elasticsearch_indices_search_fetch_time_seconds_total

# JVM Metrics
elasticsearch_jvm_memory_used_bytes
elasticsearch_jvm_gc_collection_seconds_count
elasticsearch_jvm_gc_collection_seconds_sum

# Storage Metrics
elasticsearch_filesystem_data_available_bytes
elasticsearch_indices_store_size_bytes
```

#### Logstash Metrics
```prometheus
# Pipeline Statistics
logstash_node_pipeline_events_in_total
logstash_node_pipeline_events_out_total
logstash_node_pipeline_events_filtered_total
logstash_node_pipeline_events_duration_seconds

# Queue Metrics
logstash_node_queue_events
logstash_node_queue_capacity_page_capacity_in_bytes
logstash_node_queue_max_unread_events

# JVM Metrics
logstash_node_jvm_memory_heap_used_bytes
logstash_node_jvm_gc_collection_duration_seconds
```

### Alert Configuration

```yaml
# Critical Alerts
alerts:
  - name: ElasticsearchClusterRed
    expr: elasticsearch_cluster_health_status{color="red"} == 1
    for: 2m
    labels:
      severity: critical
      team: platform
    annotations:
      summary: "Elasticsearch cluster is RED"
      description: "Cluster {{ $labels.cluster }} is in RED state"
      runbook: "https://wiki.example.com/runbooks/es-cluster-red"

  - name: ElasticsearchNodeDown
    expr: up{job="elasticsearch"} == 0
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "Elasticsearch node is down"
      description: "Node {{ $labels.instance }} is unreachable"

  - name: HighIndexingLatency
    expr: rate(elasticsearch_indices_indexing_index_time_seconds_total[5m]) > 5
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High indexing latency detected"

  - name: DiskSpaceLow
    expr: elasticsearch_filesystem_data_available_bytes / elasticsearch_filesystem_data_size_bytes < 0.1
    for: 15m
    labels:
      severity: critical
    annotations:
      summary: "Low disk space on Elasticsearch node"
      description: "Less than 10% disk space remaining on {{ $labels.instance }}"

  - name: KafkaConsumerLag
    expr: kafka_consumer_lag_sum{group="logstash-consumers"} > 100000
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High Kafka consumer lag"
```

### Monitoring Dashboards

#### Grafana Dashboard Configuration
```json
{
  "dashboard": {
    "title": "Logging Service Operations",
    "panels": [
      {
        "title": "Cluster Health",
        "targets": [{
          "expr": "elasticsearch_cluster_health_status"
        }]
      },
      {
        "title": "Indexing Rate",
        "targets": [{
          "expr": "rate(elasticsearch_indices_indexing_index_total[5m])"
        }]
      },
      {
        "title": "Search Latency",
        "targets": [{
          "expr": "histogram_quantile(0.95, elasticsearch_indices_search_query_time_seconds)"
        }]
      },
      {
        "title": "JVM Heap Usage",
        "targets": [{
          "expr": "elasticsearch_jvm_memory_heap_used_percent"
        }]
      },
      {
        "title": "Disk Usage",
        "targets": [{
          "expr": "100 - (elasticsearch_filesystem_data_available_bytes / elasticsearch_filesystem_data_size_bytes * 100)"
        }]
      },
      {
        "title": "Kafka Lag",
        "targets": [{
          "expr": "kafka_consumer_lag_sum"
        }]
      }
    ]
  }
}
```

## Performance Management

### Performance Baselines

| Metric | Target | Warning | Critical |
|--------|--------|---------|----------|
| Indexing Rate | 50k docs/sec | 30k docs/sec | 20k docs/sec |
| Search Latency (p95) | < 500ms | > 1s | > 2s |
| CPU Usage | < 70% | > 80% | > 90% |
| Memory Usage | < 75% | > 85% | > 95% |
| Disk I/O Wait | < 5% | > 10% | > 20% |
| GC Pause Time | < 100ms | > 500ms | > 1s |

### Performance Optimization

#### Index Optimization
```bash
# Force merge read-only indices
curl -X POST "localhost:9200/logs-2024.01.*/_forcemerge?max_num_segments=1"

# Update index settings for better performance
curl -X PUT "localhost:9200/logs-*/_settings" -H 'Content-Type: application/json' -d'
{
  "index": {
    "refresh_interval": "30s",
    "number_of_replicas": 1,
    "translog": {
      "durability": "async",
      "sync_interval": "30s"
    }
  }
}'

# Clear cache
curl -X POST "localhost:9200/_cache/clear"
```

#### Query Optimization
```json
// Use filters instead of queries when possible
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "status": "error" }},
        { "range": { "@timestamp": { "gte": "now-1h" }}}
      ]
    }
  }
}

// Use source filtering
{
  "_source": ["@timestamp", "message", "level"],
  "query": { ... }
}

// Limit aggregation scope
{
  "size": 0,
  "query": {
    "range": { "@timestamp": { "gte": "now-1h" }}
  },
  "aggs": { ... }
}
```

### Load Testing

```bash
# Elasticsearch load test
docker run --rm -v $(pwd)/rally:/rally elastic/rally race \
  --target-hosts=localhost:9200 \
  --pipeline=benchmark-only \
  --track=logging \
  --challenge=index-and-query

# Logstash stress test
logstash-stress-test \
  --host localhost \
  --port 5000 \
  --messages 1000000 \
  --threads 10 \
  --message-size 1024
```

## Incident Response

### Incident Classification

| Level | Response Time | Examples |
|-------|--------------|----------|
| P1 - Critical | 15 minutes | Complete cluster failure, data loss |
| P2 - High | 30 minutes | Node failures, ingestion stopped |
| P3 - Medium | 2 hours | Performance degradation, query timeouts |
| P4 - Low | 24 hours | Minor issues, cosmetic problems |

### Response Procedures

#### P1 - Cluster Failure
```bash
#!/bin/bash
# Emergency response script

# 1. Check cluster status
curl -X GET "localhost:9200/_cluster/health?pretty"

# 2. Identify failed nodes
curl -X GET "localhost:9200/_cat/nodes?v&h=ip,name,status"

# 3. Check for split brain
curl -X GET "localhost:9200/_cat/master"

# 4. Force restart if necessary
systemctl restart elasticsearch@node1

# 5. Verify shard allocation
curl -X GET "localhost:9200/_cat/shards?v&h=index,shard,prirep,state,node"

# 6. Enable allocation if disabled
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.enable": "all"
  }
}'
```

#### P2 - Node Failure
```bash
# 1. Remove failed node from cluster
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.exclude._name": "failed-node"
  }
}'

# 2. Monitor shard reallocation
watch -n 1 'curl -s localhost:9200/_cat/recovery?v'

# 3. Add replacement node
# ... deployment specific commands ...

# 4. Remove exclusion
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.exclude._name": null
  }
}'
```

### Common Issues

#### High Memory Usage
```bash
# Check memory usage per node
curl -X GET "localhost:9200/_nodes/stats/jvm?pretty"

# Clear field data cache
curl -X POST "localhost:9200/_cache/clear?fielddata=true"

# Reduce heap if needed
export ES_JAVA_OPTS="-Xms4g -Xmx4g"
systemctl restart elasticsearch
```

#### Slow Queries
```bash
# Enable slow log
curl -X PUT "localhost:9200/logs-*/_settings" -H 'Content-Type: application/json' -d'
{
  "index.search.slowlog.threshold.query.warn": "10s",
  "index.search.slowlog.threshold.query.info": "5s",
  "index.search.slowlog.threshold.fetch.warn": "1s"
}'

# Check slow log
tail -f /var/log/elasticsearch/logs-cluster_index_search_slowlog.log

# Profile specific query
curl -X GET "localhost:9200/logs-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "profile": true,
  "query": { ... }
}'
```

## Maintenance Procedures

### Daily Maintenance

```bash
#!/bin/bash
# Daily maintenance script

echo "Starting daily maintenance - $(date)"

# 1. Check and delete old indices
RETENTION_DAYS=30
cutoff_date=$(date -d "-${RETENTION_DAYS} days" +%Y.%m.%d)
curl -X DELETE "localhost:9200/logs-*-${cutoff_date}"

# 2. Force merge yesterday's indices
yesterday=$(date -d "yesterday" +%Y.%m.%d)
curl -X POST "localhost:9200/logs-*-${yesterday}/_forcemerge?max_num_segments=1"

# 3. Update aliases
curl -X POST "localhost:9200/_aliases" -H 'Content-Type: application/json' -d'
{
  "actions": [
    { "remove": { "index": "logs-*-'${cutoff_date}'", "alias": "logs-recent" }},
    { "add": { "index": "logs-*-'$(date +%Y.%m.%d)'", "alias": "logs-recent" }}
  ]
}'

# 4. Snapshot indices
curl -X PUT "localhost:9200/_snapshot/daily/snapshot_$(date +%Y%m%d)?wait_for_completion=false" -H 'Content-Type: application/json' -d'
{
  "indices": "logs-*-'${yesterday}'",
  "include_global_state": false
}'

echo "Daily maintenance completed - $(date)"
```

### Weekly Maintenance

```bash
# 1. Optimize shard allocation
curl -X GET "localhost:9200/_cluster/allocation/explain?pretty"

# 2. Rebalance if needed
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.rebalance.enable": "all"
  }
}'

# 3. Update templates
curl -X PUT "localhost:9200/_index_template/logs" -H 'Content-Type: application/json' -d @templates/logs-template.json

# 4. Clean up tasks
curl -X POST "localhost:9200/_tasks/_cancel?nodes=*&actions=*search*&age=1h"
```

### Monthly Maintenance

```bash
# 1. Major version updates
# ... follow upgrade procedures ...

# 2. Certificate renewal
./renew-certificates.sh

# 3. Performance audit
./performance-audit.sh > reports/monthly-audit-$(date +%Y%m).txt

# 4. Capacity review
./capacity-planning.sh
```

## Backup and Recovery

### Backup Strategy

```yaml
backup_policy:
  snapshot_schedule:
    hourly:
      indices: ["logs-*-today"]
      retention: 24_hours
    daily:
      indices: ["logs-*"]
      retention: 7_days
    weekly:
      indices: ["logs-*"]
      retention: 4_weeks
    monthly:
      indices: ["logs-*"]
      retention: 12_months
```

### Backup Configuration

```bash
# Configure S3 repository
curl -X PUT "localhost:9200/_snapshot/s3_backup" -H 'Content-Type: application/json' -d'
{
  "type": "s3",
  "settings": {
    "bucket": "elasticsearch-backups",
    "region": "us-east-1",
    "base_path": "logs-cluster"
  }
}'

# Create snapshot
curl -X PUT "localhost:9200/_snapshot/s3_backup/snapshot_$(date +%Y%m%d_%H%M%S)?wait_for_completion=false" -H 'Content-Type: application/json' -d'
{
  "indices": "logs-*",
  "ignore_unavailable": true,
  "include_global_state": false,
  "metadata": {
    "taken_by": "automated_backup",
    "taken_at": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"
  }
}'
```

### Recovery Procedures

```bash
# List available snapshots
curl -X GET "localhost:9200/_snapshot/s3_backup/_all?pretty"

# Restore specific indices
curl -X POST "localhost:9200/_snapshot/s3_backup/snapshot_20240115/_restore" -H 'Content-Type: application/json' -d'
{
  "indices": "logs-application-2024.01.15",
  "ignore_unavailable": true,
  "include_global_state": false,
  "rename_pattern": "logs-(.+)",
  "rename_replacement": "restored-logs-$1"
}'

# Monitor restore progress
curl -X GET "localhost:9200/_recovery?pretty&active_only=true"
```

### Disaster Recovery

```bash
#!/bin/bash
# Disaster recovery runbook

# 1. Assess damage
./assess-cluster-status.sh

# 2. Stop incoming traffic
kubectl scale deployment logstash --replicas=0

# 3. Restore from backup
latest_snapshot=$(curl -s localhost:9200/_snapshot/s3_backup/_all | jq -r '.snapshots[-1].snapshot')
curl -X POST "localhost:9200/_snapshot/s3_backup/${latest_snapshot}/_restore"

# 4. Verify data integrity
curl -X GET "localhost:9200/_cat/indices?v"

# 5. Resume traffic
kubectl scale deployment logstash --replicas=3

# 6. Validate operations
./validate-recovery.sh
```

## Security Operations

### Access Control

```bash
# Review user permissions
curl -X GET "localhost:9200/_security/user?pretty"

# Audit access logs
grep "AUTHENTICATED" /var/log/elasticsearch/logs-cluster_access.log | \
  awk '{print $5, $7, $9}' | sort | uniq -c

# Rotate API keys
curl -X POST "localhost:9200/_security/api_key" -H 'Content-Type: application/json' -d'
{
  "name": "logging-service-key-$(date +%Y%m)",
  "expiration": "30d",
  "role_descriptors": {
    "logs_writer": {
      "cluster": ["monitor"],
      "index": [{
        "names": ["logs-*"],
        "privileges": ["create_index", "index", "write"]
      }]
    }
  }
}'
```

### Security Monitoring

```yaml
security_alerts:
  - name: UnauthorizedAccess
    pattern: "AUTHENTICATION_FAILED"
    threshold: 10
    window: 5m
    
  - name: PrivilegeEscalation
    pattern: "PRIVILEGE_ESCALATION_ATTEMPT"
    threshold: 1
    window: 1m
    
  - name: SuspiciousQuery
    pattern: "script.*Runtime"
    threshold: 5
    window: 10m
```

### Compliance Auditing

```bash
# Generate compliance report
./compliance-audit.sh > reports/compliance-$(date +%Y%m).txt

# Check data retention compliance
curl -X GET "localhost:9200/_cat/indices?v" | \
  awk '$3 ~ /logs-/ {print $3, $7}' | \
  while read index age; do
    if [ $(echo $age | sed 's/d//') -gt 30 ]; then
      echo "WARNING: $index exceeds retention policy"
    fi
  done
```

## Capacity Planning

### Metrics Collection

```bash
# Storage growth rate
curl -X GET "localhost:9200/_stats/store?pretty" | \
  jq '.indices | to_entries | .[] | {index: .key, size: .value.total.store.size_in_bytes}'

# Indexing rate trends
curl -X GET "localhost:9200/_stats/indexing?pretty" | \
  jq '.indices | to_entries | .[] | {index: .key, docs_per_sec: .value.total.indexing.index_total}'

# Query load analysis
curl -X GET "localhost:9200/_nodes/stats/indices/search?pretty"
```

### Capacity Projections

```python
#!/usr/bin/env python3
# capacity_projection.py

import requests
import pandas as pd
from datetime import datetime, timedelta

def project_storage_needs():
    # Get historical data
    response = requests.get('http://localhost:9200/_stats/store')
    current_size = response.json()['_all']['total']['store']['size_in_bytes']
    
    # Calculate growth rate (simplified)
    daily_growth = current_size * 0.05  # 5% daily growth
    
    # Project 90 days
    projections = []
    for days in [30, 60, 90]:
        projected_size = current_size + (daily_growth * days)
        projections.append({
            'days': days,
            'projected_size_gb': projected_size / (1024**3),
            'required_nodes': int(projected_size / (1024**3) / 500) + 1  # 500GB per node
        })
    
    return projections

if __name__ == "__main__":
    projections = project_storage_needs()
    for p in projections:
        print(f"{p['days']} days: {p['projected_size_gb']:.2f} GB, {p['required_nodes']} nodes needed")
```

### Scaling Decisions

```yaml
scaling_triggers:
  vertical:
    - metric: cpu_usage
      threshold: 80%
      duration: 30m
      action: increase_cpu_by_2_cores
      
    - metric: memory_usage
      threshold: 85%
      duration: 30m
      action: increase_memory_by_8gb
      
  horizontal:
    - metric: storage_usage
      threshold: 75%
      action: add_data_node
      
    - metric: indexing_rate
      threshold: 100k_docs_per_sec
      action: add_ingest_node
```

## Troubleshooting Guide

### Common Problems

#### Problem: Cluster State is Yellow
```bash
# Identify unassigned shards
curl -X GET "localhost:9200/_cat/shards?v&h=index,shard,prirep,state,unassigned.reason"

# Fix allocation issues
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.enable": "all",
    "cluster.routing.rebalance.enable": "all"
  }
}'

# Retry failed allocations
curl -X POST "localhost:9200/_cluster/reroute?retry_failed=true"
```

#### Problem: High JVM Memory Pressure
```bash
# Check JVM stats
curl -X GET "localhost:9200/_nodes/stats/jvm?pretty"

# Analyze heap dump
jmap -dump:format=b,file=heap.bin $(pgrep -f elasticsearch)
jhat heap.bin

# Temporary relief
curl -X POST "localhost:9200/_cache/clear"
curl -X POST "localhost:9200/_flush"
```

#### Problem: Slow Indexing
```bash
# Check indexing stats
curl -X GET "localhost:9200/_stats/indexing?pretty"

# Identify bottlenecks
curl -X GET "localhost:9200/_nodes/hot_threads"

# Optimize bulk requests
# Adjust bulk size and refresh interval
curl -X PUT "localhost:9200/logs-*/_settings" -H 'Content-Type: application/json' -d'
{
  "index": {
    "refresh_interval": "30s",
    "translog.durability": "async"
  }
}'
```

### Debug Commands

```bash
# Cluster diagnostics
curl -X GET "localhost:9200/_cluster/stats?pretty"
curl -X GET "localhost:9200/_cluster/pending_tasks?pretty"
curl -X GET "localhost:9200/_cat/thread_pool?v&h=node_name,name,active,rejected,completed"

# Node diagnostics
curl -X GET "localhost:9200/_nodes/stats/process?pretty"
curl -X GET "localhost:9200/_nodes/stats/os?pretty"
curl -X GET "localhost:9200/_nodes/stats/fs?pretty"

# Index diagnostics
curl -X GET "localhost:9200/logs-*/_stats?pretty"
curl -X GET "localhost:9200/logs-*/_segments?pretty"
```

## Runbook Library

### Runbook: Emergency Shutdown
```bash
#!/bin/bash
# emergency_shutdown.sh

echo "EMERGENCY SHUTDOWN INITIATED - $(date)"

# 1. Stop ingestion
kubectl scale deployment logstash --replicas=0
kubectl scale deployment filebeat --replicas=0

# 2. Flush all data
curl -X POST "localhost:9200/_flush"

# 3. Disable allocation
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "persistent": {
    "cluster.routing.allocation.enable": "none"
  }
}'

# 4. Snapshot current state
curl -X PUT "localhost:9200/_snapshot/emergency/shutdown_$(date +%Y%m%d_%H%M%S)"

# 5. Graceful shutdown
systemctl stop elasticsearch

echo "EMERGENCY SHUTDOWN COMPLETED - $(date)"
```

### Runbook: Performance Emergency
```bash
#!/bin/bash
# performance_emergency.sh

echo "PERFORMANCE EMERGENCY RESPONSE - $(date)"

# 1. Reduce load
curl -X PUT "localhost:9200/*/_settings" -H 'Content-Type: application/json' -d'
{
  "index.search.throttled": true
}'

# 2. Clear caches
curl -X POST "localhost:9200/_cache/clear"

# 3. Cancel long-running tasks
curl -X POST "localhost:9200/_tasks/_cancel?actions=*search*&nodes=*"

# 4. Temporary disable features
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "indices.memory.index_buffer_size": "5%",
    "indices.queries.cache.size": "5%"
  }
}'

echo "Emergency measures applied. Monitor cluster health."
```

## Contact Information

### Escalation Matrix

| Level | Contact | Response Time |
|-------|---------|---------------|
| L1 | NOC Team | 24/7 |
| L2 | Platform Team | Business hours |
| L3 | Engineering Lead | On-call |
| Vendor | Elastic Support | Per contract |

### On-Call Rotation
- Primary: platform-oncall@example.com
- Secondary: platform-backup@example.com
- Manager: platform-manager@example.com

For detailed procedures and additional runbooks, refer to the internal wiki.