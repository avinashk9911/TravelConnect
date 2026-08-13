# TravelConnect — System Architecture

## Overview

TravelConnect is a corporate travel booking platform that connects a booking system to multiple external travel suppliers. The platform demonstrates enterprise integration patterns commonly used in the travel technology industry.

## High-Level Architecture

```mermaid
graph TD
    FE["React Frontend\n:3000"]
    TS["Traveler Service\n:8081"]
    BS["Booking Service\n:8082"]
    IS["Integration Service\n:8083"]
    NS["Notification Service\n:8084"]
    RMQ["RabbitMQ\n:5672 / :15672"]
    PG[("PostgreSQL\n:5432")]
    FS["Flight Supplier\n:9001 REST/JSON"]
    HS["Hotel Supplier\n:9002 REST/JSON"]
    CS["Car Supplier\n:9003 SOAP/XML"]
    LM["AWS Lambda\nBooking Audit"]
    DDB["DynamoDB\nAudit Store"]

    FE -->|HTTP REST| BS
    FE -->|HTTP REST| TS
    FE -->|HTTP REST| IS

    BS -->|REST| TS
    BS -->|JPA| PG
    TS -->|JPA| PG
    IS -->|JPA| PG

    BS -->|BookingCreated event| RMQ
    IS -->|SupplierResponseReceived event| RMQ
    NS -->|BookingCompleted event consumed| RMQ

    RMQ -->|booking.created queue| IS
    RMQ -->|supplier.response queue| BS
    RMQ -->|booking.completed queue| NS

    IS -->|REST/JSON| FS
    IS -->|REST/JSON| HS
    IS -->|SOAP/XML| CS

    NS -->|invoke| LM
    LM -->|PutItem| DDB
```

## Component Responsibilities

| Component | Port | Responsibility |
|---|---|---|
| React Frontend | 3000 | User interface — traveler profiles, search, bookings, admin |
| Traveler Service | 8081 | Manages traveler profiles and travel preferences |
| Booking Service | 8082 | Owns trips, bookings, search; publishes events |
| Integration Service | 8083 | Routes requests to suppliers; adapts internal ↔ supplier formats |
| Notification Service | 8084 | Consumes completion events; triggers Lambda audit |
| Flight Supplier (mock) | 9001 | Simulates airline REST/JSON API |
| Hotel Supplier (mock) | 9002 | Simulates hotel REST/JSON API |
| Car Supplier (mock) | 9003 | Simulates car rental SOAP/XML API |
| PostgreSQL | 5432 | Transactional data store |
| RabbitMQ | 5672 | Asynchronous message broker |
| AWS Lambda | — | Serverless audit event processor |
| DynamoDB | — | Audit record store |

## Key Design Decisions

### Why separate services?

Each service has a single bounded context and can be deployed, scaled, and maintained independently:

- **Traveler Service** — profile management is a separate concern from booking logic. It could be replaced with an external Identity/HR system without touching the booking flow.
- **Booking Service** — owns the business lifecycle of a trip. Doesn't know about supplier protocols.
- **Integration Service** — isolates all supplier-specific complexity. Adding a new supplier means adding one new adapter, nothing else.
- **Notification Service** — side effects (email, audit) belong outside the core booking flow.

### Why RabbitMQ (not synchronous calls)?

Calling suppliers synchronously would mean the booking API response waits for three supplier calls (potentially 1-2 seconds each). With RabbitMQ:
- The booking API responds immediately after recording the booking (< 100ms)
- Supplier calls happen in the background
- If a supplier is slow or down, the booking still succeeds — it just stays in PROCESSING status

### Why PostgreSQL for transactional data?

Bookings involve multiple related records (Trip → Booking → BookingItems) that must be written atomically. PostgreSQL's ACID guarantees ensure a booking is never partially saved. DynamoDB is used instead for the audit log because those records are write-once and don't need relational integrity.

### Why the Adapter pattern in Integration Service?

The booking system sends a canonical `SupplierBookingRequest` regardless of supplier. Each adapter converts this to the supplier's specific format:

```
BookingRequest (internal)
     |
     +─→ FlightSupplierAdapter → { JSON request for flight supplier }
     +─→ HotelSupplierAdapter  → { JSON request for hotel supplier }
     +─→ CarSupplierAdapter    → { SOAP/XML for car supplier }
```

This means the booking system is completely decoupled from supplier protocols. Changing a supplier's API only requires modifying its adapter.

## Data Flow: Booking Creation

```mermaid
sequenceDiagram
    participant Client
    participant BS as Booking Service
    participant RMQ as RabbitMQ
    participant IS as Integration Service
    participant Supplier
    participant NS as Notification Service
    participant Lambda

    Client->>BS: POST /api/v1/bookings
    BS->>BS: Validate + save Booking (PENDING)
    BS->>RMQ: Publish BookingCreated event
    BS->>Client: 201 Created (bookingRef, status=PENDING)

    RMQ->>IS: BookingCreated event
    IS->>IS: Create IntegrationRequest (PENDING)
    IS->>Supplier: Send booking request
    Supplier-->>IS: Response
    IS->>IS: Save IntegrationResponse
    IS->>RMQ: Publish SupplierResponseReceived event

    RMQ->>BS: SupplierResponseReceived event
    BS->>BS: Update Booking status → CONFIRMED
    BS->>RMQ: Publish BookingCompleted event

    RMQ->>NS: BookingCompleted event
    NS->>Lambda: Invoke with audit payload
    Lambda->>Lambda: Save DynamoDB record
```

## Technology Mapping

| Need | Technology | Why |
|---|---|---|
| Core backend | Java 21 + Spring Boot 3.x | Industry standard for enterprise Java |
| Database | PostgreSQL + Flyway | ACID transactions + versioned migrations |
| Async messaging | RabbitMQ | Decouples supplier calls from booking API |
| SOAP integration | XML + WSDL/XSD | Legacy enterprise suppliers still use SOAP |
| Serverless audit | AWS Lambda + DynamoDB | Zero-maintenance audit store |
| Infra-as-code | AWS CDK (TypeScript) | Type-safe cloud infrastructure |
| Containerisation | Docker + Compose | Consistent local-to-production environments |
| CI/CD | Jenkins + GitHub | Industry-standard pipeline |
| Code quality | SonarQube | Static analysis, bug detection |
| API documentation | Spring Actuator + this guide | Operational visibility |
