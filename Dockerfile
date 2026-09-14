# ==========================================
# Stage 1: Build & Package (Maven + JDK 21)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (enables layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Production Runtime (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="Supplier Data Platform Team"
LABEL description="Container image for Supplier Data Platform Spring Boot microservice"

WORKDIR /app

# Create a non-root dedicated application user and group for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create dropzone directories for local fallback processing
RUN mkdir -p /app/dropzone /app/ftp-dropzone /app/azure-datalake-dropzone \
    && chown -R appuser:appgroup /app

# Copy executable jar artifact from builder stage
COPY --from=builder /app/target/supplier-data-platform-1.0.0.jar app.jar
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose HTTP application port
EXPOSE 8080

# Environment variables with sensible defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck configuration querying Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entrypoint executing the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
