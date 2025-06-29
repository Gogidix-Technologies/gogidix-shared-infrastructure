# Multi-stage build for Service Registry
FROM eclipse-temurin:17-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine AS production

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 -S serviceregistry && \
    adduser -S serviceregistry -u 1001 -G serviceregistry

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown -R serviceregistry:serviceregistry /app

# Switch to non-root user
USER serviceregistry

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# Entry point
ENTRYPOINT ["java", "-jar", "app.jar"]