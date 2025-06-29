FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Create uploads and temp directories
RUN mkdir -p uploads temp

# Add application jar
COPY target/file-storage-service-1.0.0.jar file-storage-service.jar

# Create a non-root user for security
RUN groupadd -r filestorage && useradd -r -g filestorage filestorage
RUN chown -R filestorage:filestorage /app
USER filestorage

# Expose port
EXPOSE 8085

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8085/api/files/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar file-storage-service.jar"]
