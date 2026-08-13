# ── Multi-stage Docker build ──────────────────────────────────────────────────
#
# Stage 1: Build — uses a full JDK image to compile the application
# Stage 2: Run  — uses a minimal JRE image to run it
#
# Why multi-stage?
# The build image needs Maven + JDK (large). The final image only needs the JRE
# and the compiled jar — much smaller, reducing attack surface and startup time.

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy parent POM first (Docker cache layer — only re-runs if pom.xml changes)
COPY ../../pom.xml ./parent-pom.xml
COPY pom.xml ./pom.xml

# Download dependencies (cached unless pom.xml changes)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user for security — never run as root in production
RUN addgroup -S travelconnect && adduser -S travelconnect -G travelconnect

WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=build /workspace/target/*.jar app.jar

# Set correct ownership
RUN chown travelconnect:travelconnect app.jar

USER travelconnect

# JVM tuning for containers:
# -XX:+UseContainerSupport   — respect container CPU/memory limits (Java 10+)
# -XX:MaxRAMPercentage=75    — use up to 75% of container memory for heap
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
