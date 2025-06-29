#!/bin/bash

# Configuration script for all shared infrastructure services
BASE_DIR="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/CLEAN-SOCIAL-ECOMMERCE-ECOSYSTEM/shared-infrastructure"

# Service ports mapping
declare -A PORTS
PORTS=(
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
)

# Function to update application.yml for a service
update_application_yml() {
    local service=$1
    local port=${PORTS[$service]}
    
    if [ -z "$port" ]; then
        echo "No port configured for $service"
        return 1
    fi
    
    local app_yml="$BASE_DIR/$service/src/main/resources/application.yml"
    
    echo "Updating $app_yml for $service"
    
    cat > "$app_yml" << EOF
server:
  port: $port

spring:
  application:
    name: $service
  datasource:
    url: jdbc:h2:mem:${service//-/}db
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    hostname: localhost

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.exalt: INFO
    org.springframework: INFO
EOF
}

# Function to update Application.java to include @EnableDiscoveryClient
update_application_java() {
    local service=$1
    local java_file=$(find "$BASE_DIR/$service/src/main/java" -name "Application.java" 2>/dev/null | head -1)
    
    if [ -z "$java_file" ]; then
        echo "No Application.java found for $service"
        return 1
    fi
    
    echo "Updating $java_file for $service"
    
    # Check if already has @EnableDiscoveryClient
    if grep -q "@EnableDiscoveryClient" "$java_file"; then
        echo "@EnableDiscoveryClient already present in $service"
        return 0
    fi
    
    # Add import and annotation
    sed -i '/import org.springframework.boot.autoconfigure.SpringBootApplication;/a import org.springframework.cloud.client.discovery.EnableDiscoveryClient;' "$java_file"
    sed -i '/^@SpringBootApplication/a @EnableDiscoveryClient' "$java_file"
    
    echo "Added @EnableDiscoveryClient to $service"
}

# Function to ensure Eureka dependencies in pom.xml
update_pom_xml() {
    local service=$1
    local pom_file="$BASE_DIR/$service/pom.xml"
    
    if [ ! -f "$pom_file" ]; then
        echo "No pom.xml found for $service"
        return 1
    fi
    
    echo "Checking dependencies for $service"
    
    # Check if eureka client dependency exists
    if grep -q "spring-cloud-starter-netflix-eureka-client" "$pom_file"; then
        echo "Eureka client dependency already present in $service"
        return 0
    fi
    
    # Add dependencies before </dependencies>
    sed -i '/<\/dependencies>/i \
        <!-- Spring Cloud Eureka Client -->\
        <dependency>\
            <groupId>org.springframework.cloud</groupId>\
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>\
        </dependency>\
\
        <!-- Spring Data JPA -->\
        <dependency>\
            <groupId>org.springframework.boot</groupId>\
            <artifactId>spring-boot-starter-data-jpa</artifactId>\
        </dependency>\
\
        <!-- H2 Database -->\
        <dependency>\
            <groupId>com.h2database</groupId>\
            <artifactId>h2</artifactId>\
            <scope>runtime</scope>\
        </dependency>\
' "$pom_file"
    
    echo "Added Eureka dependencies to $service"
}

# Main execution
echo "Configuring all shared infrastructure services for Eureka integration..."

for service in "${!PORTS[@]}"; do
    if [ ! -d "$BASE_DIR/$service" ]; then
        echo "Service directory not found: $service"
        continue
    fi
    
    echo "Configuring $service..."
    
    # Update configuration files
    update_application_yml "$service"
    update_application_java "$service"
    update_pom_xml "$service"
    
    echo "Configured $service"
    echo "----------------------------------------"
done

echo "Configuration complete!"