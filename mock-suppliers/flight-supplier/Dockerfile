# Mock Flight Supplier — multi-stage Docker build

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY ../../pom.xml ./parent-pom.xml
COPY pom.xml ./pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S travelconnect && adduser -S travelconnect -G travelconnect

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

RUN chown travelconnect:travelconnect app.jar

USER travelconnect

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 9001

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
