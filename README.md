# TravelConnect — Travel Booking & Supplier Integration Platform

A portfolio-grade backend platform demonstrating enterprise Java engineering patterns used in the travel technology industry.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://postgresql.org)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange)](https://rabbitmq.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)](https://docker.com)

---

## Business Problem

Corporate travel management platforms must connect to dozens of external travel suppliers — airlines, hotels, car rental companies. Each supplier has its own API protocol (REST/JSON or SOAP/XML), its own authentication, and its own request/response format. A naive approach — calling suppliers synchronously from the booking API — results in slow responses and cascading failures if a supplier is down.

TravelConnect solves this with an **asynchronous integration layer**: bookings are recorded immediately, and supplier calls happen in the background via a message queue. The booking status updates as responses arrive.

---

## Architecture

```
React Frontend :3000
        │
        ▼
 ┌─────────────────────────────────────┐
 │        Booking Service :8082         │
 │  (trips, bookings, search, events)  │
 └────────────────┬────────────────────┘
                  │ RabbitMQ events
        ┌─────────┴────────────┐
        ▼                      ▼
 Traveler Service     Integration Service :8083
     :8081            (adapter pattern, suppliers)
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        Flight Supplier  Hotel Supplier  Car Supplier
         :9001 REST/JSON  :9002 REST/JSON  :9003 SOAP/XML
                              │
                              ▼ (BookingCompleted event)
                      Notification Service :8084
                              │
                              ▼
                         AWS Lambda
                              │
                              ▼
                    DynamoDB Audit Store
```

**PostgreSQL** stores all transactional data. **RabbitMQ** decouples booking creation from supplier calls. **AWS Lambda + DynamoDB** provide a serverless audit trail.

---

## Services

| Service | Port | Responsibility |
|---|---|---|
| **Traveler Service** | 8081 | Manage traveler profiles, passport info, preferences |
| **Booking Service** | 8082 | Own trips and bookings; publish booking events |
| **Integration Service** | 8083 | Route to suppliers; adapt internal ↔ supplier formats |
| **Notification Service** | 8084 | Consume completion events; invoke Lambda audit |
| **Flight Supplier (mock)** | 9001 | Simulate airline REST/JSON API |
| **Hotel Supplier (mock)** | 9002 | Simulate hotel REST/JSON API |
| **Car Supplier (mock)** | 9003 | Simulate car rental SOAP/XML API |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (records, pattern matching) |
| Framework | Spring Boot 3.3.x |
| Database | PostgreSQL 16 + Flyway migrations |
| ORM | Spring Data JPA + Hibernate |
| Messaging | RabbitMQ (AMQP) |
| Integration | REST/JSON (Flight, Hotel) + SOAP/XML (Car) |
| Serverless | AWS Lambda + DynamoDB + CDK (TypeScript) |
| Container | Docker + Docker Compose |
| CI/CD | Jenkins + GitHub |
| Code Quality | SonarQube |
| Frontend | React 18 + TypeScript + Vite |

---

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop
- Node.js 20+ (for frontend)

### 1. Start infrastructure

```bash
docker compose -f docker/docker-compose.infra.yml up -d
```

This starts PostgreSQL (:5432) and RabbitMQ (:5672, management UI :15672).

### 2. Start backend services

```bash
# Each in a separate terminal
cd services/traveler-service && mvn spring-boot:run
cd services/booking-service  && mvn spring-boot:run
cd services/integration-service && mvn spring-boot:run
cd services/notification-service && mvn spring-boot:run
```

### 3. Start mock suppliers

```bash
cd mock-suppliers/flight-supplier && mvn spring-boot:run
cd mock-suppliers/hotel-supplier  && mvn spring-boot:run
cd mock-suppliers/car-supplier    && mvn spring-boot:run
```

### 4. Start frontend

```bash
cd frontend && npm install && npm run dev
# Open http://localhost:3000
```

### OR — Full Docker Compose

```bash
docker compose -f docker/docker-compose.yml up --build
# Frontend: http://localhost:3000
```

---

## End-to-End Flow

```bash
# 1. Create a traveler
curl -s -X POST http://localhost:8081/api/v1/travelers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Smith","email":"john@example.com"}' | python3 -m json.tool

# 2. Create a trip (use the travelerId from step 1)
curl -s -X POST http://localhost:8082/api/v1/trips \
  -H "Content-Type: application/json" \
  -d '{"travelerId":"<id>","name":"NYC Trip","destination":"New York","startDate":"2025-06-01","endDate":"2025-06-07"}' | python3 -m json.tool

# 3. Create a booking (use tripId from step 2)
curl -s -X POST http://localhost:8082/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "tripId":"<tripId>",
    "travelerId":"<travelerId>",
    "items":[{"itemType":"FLIGHT","origin":"LHR","destination":"JFK","departureDate":"2025-06-01","passengers":1,"pricePerUnit":299.99,"quantity":1}]
  }' | python3 -m json.tool

# 4. Poll booking status — watch it go PENDING → CONFIRMED
curl -s http://localhost:8082/api/v1/bookings/<bookingId>/status | python3 -m json.tool

# 5. Check integration requests (supplier calls)
curl -s http://localhost:8083/api/v1/integrations/booking/<bookingId> | python3 -m json.tool

# 6. Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## Running Tests

```bash
# Unit tests (no Docker)
mvn test

# All tests including integration (Docker required)
mvn verify

# Single service
cd services/traveler-service && mvn test
```

---

## Key Design Patterns

### 1. Adapter Pattern (Integration Service)

Each supplier adapter converts the internal canonical model to the supplier's specific format:

```
SupplierBookingRequest (internal)
     │
     ├── FlightSupplierAdapter → REST/JSON to flight-supplier:9001
     ├── HotelSupplierAdapter  → REST/JSON to hotel-supplier:9002
     └── CarSupplierAdapter    → SOAP/XML to car-supplier:9003
```

Adding a new supplier = adding one new adapter class. Zero changes to booking logic.

### 2. Event-Driven Asynchrony (RabbitMQ)

```
Booking API → BookingCreated event → RabbitMQ → Integration Service
                                                  (async, non-blocking)
```

The booking API responds in < 100ms. Supplier calls happen in the background.

### 3. Dead Letter Queue (Resilience)

```
Supplier unavailable → Retry (3 attempts, 2s/4s/8s backoff) → DLQ
```

Failed messages are preserved in the DLQ for manual inspection and replay.

### 4. Distributed Tracing

Every booking has a `traceId` that propagates through all RabbitMQ events to the DynamoDB audit record. Use `grep traceId=<value>` across all service logs to trace a complete booking.

---

## Monitoring

| Endpoint | Description |
|---|---|
| `:808x/actuator/health` | Service health + dependencies |
| `:808x/actuator/metrics/jvm.memory.used` | Heap usage |
| `:808x/actuator/metrics/http.server.requests` | Request latencies |
| `:808x/actuator/threaddump` | Thread state (debug blocked threads) |
| `:808x/actuator/heapdump` | Heap snapshot for memory analysis |
| `localhost:15672` | RabbitMQ management UI |

---

## Project Structure

```
travelconnect/
├── pom.xml                              ← Parent POM (Java 21, dependency versions)
├── services/
│   ├── traveler-service/               ← Traveler profiles (port 8081)
│   ├── booking-service/                ← Trips + bookings + RabbitMQ (port 8082)
│   ├── integration-service/            ← Supplier adapters + RabbitMQ (port 8083)
│   └── notification-service/           ← Event consumer + Lambda trigger (port 8084)
├── mock-suppliers/
│   ├── flight-supplier/                ← Mock airline REST API (port 9001)
│   ├── hotel-supplier/                 ← Mock hotel REST API (port 9002)
│   └── car-supplier/                   ← Mock car SOAP/XML API (port 9003)
├── aws/
│   ├── travelconnect-infrastructure/   ← CDK TypeScript project
│   └── lambda/booking-audit/           ← Lambda function (Node.js)
├── frontend/                           ← React + TypeScript (port 3000)
├── docker/
│   ├── docker-compose.yml              ← Full stack
│   └── docker-compose.infra.yml        ← PostgreSQL + RabbitMQ only
├── jenkins/
│   └── Jenkinsfile                     ← CI/CD pipeline
├── docs/
│   ├── architecture.md                 ← System design + Mermaid diagrams
│   ├── er-diagram.md                   ← Database schema
│   ├── sequence-diagrams.md            ← Key flows
│   ├── api-guide.md                    ← REST API reference
│   ├── troubleshooting.md              ← Debug guide
│   ├── deployment.md                   ← Run locally + AWS + K8s
│   └── interview-guide.md              ← Technology explanations for interviews
├── sonar-project.properties            ← SonarQube config
└── README.md
```

---

## Documentation

- [Architecture](docs/architecture.md)
- [ER Diagram](docs/er-diagram.md)
- [Sequence Diagrams](docs/sequence-diagrams.md)
- [API Reference](docs/api-guide.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Deployment](docs/deployment.md)
- [Interview Guide](docs/interview-guide.md)

---

## Future Improvements

- Spring Security with JWT (ADMIN/TRAVELER roles)
- Spring Cloud Gateway as API gateway
- Distributed tracing with Micrometer + Zipkin
- Kubernetes deployment manifests
- API rate limiting
- Real supplier integrations (Amadeus, Sabre GDS)
