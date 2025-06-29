# Multi-stage build for Translation Service
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
RUN groupadd -r translation && useradd -r -g translation translation

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/logs /app/cache /app/dictionaries && \
    chown -R translation:translation /app

# Switch to non-root user
USER translation

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8094/actuator/health || exit 1

# Expose ports
EXPOSE 8094 9090

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]