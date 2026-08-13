# TravelConnect — Deployment Guide

## Local Development (fastest iteration)

Start only infrastructure, run services from your IDE:

```bash
# 1. Start PostgreSQL + RabbitMQ
docker compose -f docker/docker-compose.infra.yml up -d

# 2. Run each service from IDE or terminal
cd services/traveler-service && mvn spring-boot:run
cd services/booking-service && mvn spring-boot:run
cd services/integration-service && mvn spring-boot:run
cd services/notification-service && mvn spring-boot:run

# 3. Run mock suppliers
cd mock-suppliers/flight-supplier && mvn spring-boot:run
cd mock-suppliers/hotel-supplier && mvn spring-boot:run
cd mock-suppliers/car-supplier && mvn spring-boot:run

# 4. Run frontend (separate terminal)
cd frontend && npm install && npm run dev
```

## Full Stack with Docker Compose

```bash
# Build and start everything
docker compose -f docker/docker-compose.yml up --build

# Start in background
docker compose -f docker/docker-compose.yml up --build -d

# View logs
docker compose -f docker/docker-compose.yml logs -f

# Stop everything
docker compose -f docker/docker-compose.yml down

# Stop and remove volumes (clean slate)
docker compose -f docker/docker-compose.yml down -v
```

Services available at:
- Frontend: http://localhost:3000
- Traveler Service: http://localhost:8081
- Booking Service: http://localhost:8082
- Integration Service: http://localhost:8083
- Notification Service: http://localhost:8084
- Flight Supplier (mock): http://localhost:9001
- Hotel Supplier (mock): http://localhost:9002
- Car Supplier (mock): http://localhost:9003
- RabbitMQ UI: http://localhost:15672

## Running Tests

```bash
# All unit tests (no Docker needed)
mvn test

# All tests including integration tests (Docker required for Testcontainers)
mvn verify

# Single service tests
cd services/traveler-service && mvn test

# Skip tests (faster build)
mvn package -DskipTests
```

## AWS Deployment (Production)

### Prerequisites
- AWS CLI configured: `aws configure`
- Node.js 20+ for CDK
- Docker for building Lambda layers

### Deploy infrastructure
```bash
cd aws/travelconnect-infrastructure

# Install CDK dependencies
npm install

# Bootstrap CDK (first time only per AWS account/region)
npx cdk bootstrap

# Preview changes
npx cdk diff

# Deploy
npx cdk deploy
```

### Deploy Lambda function
```bash
cd aws/lambda/booking-audit
npm install
zip -r function.zip .
aws lambda update-function-code \
  --function-name travelconnect-booking-audit \
  --zip-file fileb://function.zip
```

### Enable AWS integration in Notification Service
Set environment variables:
```
AWS_ENABLED=true
AWS_REGION=eu-west-1
LAMBDA_FUNCTION_NAME=travelconnect-booking-audit
```

Ensure the EC2/ECS role or IAM user running the service has `lambda:InvokeFunction` permission on the function ARN.

## Jenkins CI/CD

### Setup
1. Install Jenkins with Docker and Maven tools configured
2. Add SonarQube server in Jenkins → Manage Jenkins → Configure System
3. Create a Pipeline job pointing at the Jenkinsfile in this repo
4. Configure Docker registry credentials

### Pipeline stages
1. **Checkout** — pulls code from GitHub
2. **Compile** — `mvn clean compile`
3. **Unit Tests** — `mvn test` + publishes JUnit results
4. **Integration Tests** — `mvn verify` with Testcontainers (Docker required)
5. **Code Analysis** — SonarQube scan
6. **Quality Gate** — fails build if quality thresholds not met
7. **Package** — `mvn package -DskipTests` + archives jars
8. **Docker Build** — builds images for all services (main/develop branches only)

## Kubernetes (Basic Manifests)

For Kubernetes exposure, create a `k8s/` directory with:

```yaml
# k8s/traveler-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: traveler-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: traveler-service
  template:
    metadata:
      labels:
        app: traveler-service
    spec:
      containers:
      - name: traveler-service
        image: your-registry/travelconnect/traveler-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: travelconnect-db-secret
              key: host
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 15
```

Note: The project includes basic Kubernetes manifests for portfolio demonstration. A production Kubernetes environment would also need: ConfigMaps, Secrets, Services, Ingress, HorizontalPodAutoscaler, and PersistentVolumeClaims.
