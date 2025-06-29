#!/bin/bash

# Shared Infrastructure Services Startup Script
BASE_DIR="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/shared-infrastructure"
EUREKA_URL="http://localhost:8761/eureka/"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Service configuration
declare -A SERVICES
SERVICES=(
    ["service-registry"]="8761"
    ["api-gateway"]="8080"
    ["auth-service"]="8081"
    ["user-profile-service"]="8082"
    ["notification-service"]="8083"
    ["file-storage-service"]="8084"
    ["document-verification"]="8085"
    ["kyc-service"]="8086"
    ["geo-location-service"]="8087"
    ["caching-service"]="8088"
    ["logging-service"]="8089"
    ["analytics-engine"]="8090"
    ["admin-frameworks"]="8091"
    ["payment-processing-service"]="8092"
    ["currency-exchange-service"]="8093"
    ["feature-flag-service"]="8094"
    ["translation-service"]="8095"
    ["monitoring-service"]="8096"
    ["config-server"]="8097"
    ["ui-design-system"]="8098"
    ["tracing-config"]="8099"
    ["message-broker"]="9092"
    ["billing-engine"]="3000"
)

log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

# Function to check if a service has Java/Maven structure
is_java_service() {
    local service=$1
    [ -f "$BASE_DIR/$service/pom.xml" ]
}

# Function to check if a service has Node.js structure
is_node_service() {
    local service=$1
    [ -f "$BASE_DIR/$service/package.json" ]
}

# Function to compile Java service
compile_java_service() {
    local service=$1
    local port=${SERVICES[$service]}
    
    log "Compiling Java service: $service"
    cd "$BASE_DIR/$service"
    
    if mvn clean compile -q; then
        log "✓ Successfully compiled $service"
        return 0
    else
        error "✗ Failed to compile $service"
        return 1
    fi
}

# Function to compile Node.js service
compile_node_service() {
    local service=$1
    
    log "Installing dependencies for Node.js service: $service"
    cd "$BASE_DIR/$service"
    
    if npm install --silent; then
        log "✓ Successfully installed dependencies for $service"
        return 0
    else
        error "✗ Failed to install dependencies for $service"
        return 1
    fi
}

# Function to start Java service
start_java_service() {
    local service=$1
    local port=${SERVICES[$service]}
    
    log "Starting Java service: $service on port $port"
    cd "$BASE_DIR/$service"
    
    # Check if already running
    if lsof -i :$port >/dev/null 2>&1; then
        warn "Service $service already running on port $port"
        return 0
    fi
    
    nohup mvn spring-boot:run > "$service.log" 2>&1 &
    local pid=$!
    echo $pid > "$service.pid"
    
    log "✓ Started $service (PID: $pid)"
    return 0
}

# Function to start Node.js service
start_node_service() {
    local service=$1
    local port=${SERVICES[$service]}
    
    log "Starting Node.js service: $service on port $port"
    cd "$BASE_DIR/$service"
    
    # Check if already running
    if lsof -i :$port >/dev/null 2>&1; then
        warn "Service $service already running on port $port"
        return 0
    fi
    
    nohup npm start > "$service.log" 2>&1 &
    local pid=$!
    echo $pid > "$service.pid"
    
    log "✓ Started $service (PID: $pid)"
    return 0
}

# Function to check if service is healthy
check_health() {
    local service=$1
    local port=${SERVICES[$service]}
    local max_attempts=30
    local attempt=1
    
    log "Checking health for $service on port $port"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            log "✓ $service is healthy"
            return 0
        fi
        
        sleep 2
        ((attempt++))
    done
    
    warn "✗ $service health check failed after $max_attempts attempts"
    return 1
}

# Function to wait for Eureka registration
wait_for_eureka_registration() {
    local service=$1
    local service_name=$(echo $service | tr '[:lower:]' '[:upper:]' | tr '-' '_')
    local max_attempts=30
    local attempt=1
    
    log "Waiting for $service to register with Eureka"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$EUREKA_URL../apps" | grep -q "$service_name"; then
            log "✓ $service registered with Eureka"
            return 0
        fi
        
        sleep 2
        ((attempt++))
    done
    
    warn "✗ $service failed to register with Eureka after $max_attempts attempts"
    return 1
}

# Main execution
main() {
    log "Starting all shared infrastructure services..."
    
    # Ensure Eureka is running first
    if ! curl -s http://localhost:8761/actuator/health >/dev/null 2>&1; then
        error "Eureka server is not running. Please start service-registry first."
        exit 1
    fi
    
    local compiled_count=0
    local started_count=0
    local registered_count=0
    
    # Process each service
    for service in "${!SERVICES[@]}"; do
        # Skip already running services
        if [ "$service" = "service-registry" ] || [ "$service" = "api-gateway" ] || [ "$service" = "auth-service" ]; then
            log "Skipping $service (already running)"
            continue
        fi
        
        if [ ! -d "$BASE_DIR/$service" ]; then
            warn "Service directory not found: $service"
            continue
        fi
        
        log "Processing service: $service"
        
        # Compile service
        if is_java_service "$service"; then
            if compile_java_service "$service"; then
                ((compiled_count++))
                
                # Start service
                if start_java_service "$service"; then
                    ((started_count++))
                    
                    # Wait a bit for startup
                    sleep 10
                    
                    # Check if it registered with Eureka (skip for special services)
                    if [[ ! "$service" =~ ^(message-broker|billing-engine|ui-design-system)$ ]]; then
                        if wait_for_eureka_registration "$service"; then
                            ((registered_count++))
                        fi
                    fi
                fi
            fi
        elif is_node_service "$service"; then
            if compile_node_service "$service"; then
                ((compiled_count++))
                
                # Start service
                if start_node_service "$service"; then
                    ((started_count++))
                fi
            fi
        else
            warn "Unknown service type for $service"
        fi
        
        log "----------------------------------------"
    done
    
    log "Summary:"
    log "Compiled services: $compiled_count"
    log "Started services: $started_count"
    log "Registered with Eureka: $registered_count"
    
    # Show final Eureka status
    log "Current services registered with Eureka:"
    curl -s "$EUREKA_URL../apps" | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort
}

# Run main function
main "$@"