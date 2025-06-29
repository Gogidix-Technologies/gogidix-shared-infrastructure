# Multi-stage build for Tracing Configuration Service
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r tracing && useradd -r -g tracing tracing

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/logs /app/config && chown -R tracing:tracing /app

# Copy configuration files
COPY src/main/resources/sampling_strategies.json /app/config/

# Switch to non-root user
USER tracing

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8093/actuator/health || exit 1

# Expose ports
EXPOSE 8093 9090 14268 14250 6831 6832

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]