# Multi-stage build for TradeScout with Java 25
FROM eclipse-temurin:25-jdk-alpine AS builder

LABEL maintainer="TradeScout"
LABEL description="Professional Trading Opportunity Scanner"
LABEL java.version="25"

WORKDIR /app

# Copy Maven files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source
COPY src ./src

# Build application with preview features enabled
RUN ./mvnw clean package -DskipTests

# Runtime stage with Java 25
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create user
RUN addgroup -g 1000 tradescout && \
    adduser -D -u 1000 -G tradescout tradescout

# Copy JAR
COPY --from=builder /app/target/tradescout.jar app.jar

# Create directories
RUN mkdir -p /app/logs && chown -R tradescout:tradescout /app

USER tradescout

# JVM Options - optimized for Java 25 with ZGC
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseZGC -XX:+ZGenerational --enable-preview"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]