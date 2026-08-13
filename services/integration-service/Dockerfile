# ── Multi-stage Docker build ──────────────────────────────────────────────────
#
# Stage 1: Build — compiles the application using a full JDK image
# Stage 2: Run  — runs the jar using a minimal JRE image
#
# The two-stage approach keeps the final image lean: the build tools (Maven,
# JDK source files) are discarded and only the fat jar reaches production.

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy parent POM first — Docker cache layer invalidated only if pom.xml changes
COPY ../../pom.xml ./parent-pom.xml
COPY pom.xml ./pom.xml

# Download dependencies (cached layer unless pom.xml changes)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Copy source and build the fat jar
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Run as non-root for security
RUN addgroup -S travelconnect && adduser -S travelconnect -G travelconnect

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

RUN chown travelconnect:travelconnect app.jar

USER travelconnect

# -XX:+UseContainerSupport  — respect container CPU/memory limits
# -XX:MaxRAMPercentage=75   — use up to 75% of container RAM for heap
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8083

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
