# Multi-stage build for Admin Framework
# Stage 1: Build the JAR
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw.cmd ./
COPY pom.xml ./

# Copy source code
COPY src/ ./src/

# Build the application
RUN chmod +x mvnw.cmd && ./mvnw.cmd clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jre-slim

WORKDIR /app

# Install essential tools for cloud deployment
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Cloud-ready configuration
ENV SPRING_PROFILES_ACTIVE=cloud
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=cloud"

# Health check for cloud orchestration
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]