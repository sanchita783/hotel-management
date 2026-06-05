# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: Run
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

# Create non-root user
RUN groupadd --system hotelapp && \
    useradd --system --gid hotelapp --shell /bin/false hotelapp

WORKDIR /app

# Create log directory
RUN mkdir -p /var/log/hotel-management && \
    chown hotelapp:hotelapp /var/log/hotel-management

# Copy JAR from builder
COPY --from=builder /app/target/hotel-management-1.0.0.jar app.jar

# Set ownership
RUN chown hotelapp:hotelapp app.jar

USER hotelapp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
