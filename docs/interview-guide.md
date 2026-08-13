# TravelConnect — Interview Explanation Guide

This guide helps you explain the project confidently during a technical interview. For each technology, there is: **what it does**, **why it's here**, and **a sample answer**.

---

## "Walk me through the project"

> "TravelConnect is a corporate travel booking platform I built as a portfolio project to demonstrate enterprise Java engineering. The core problem it solves is connecting a booking system to multiple external travel suppliers — airlines, hotels, and car rental companies — each with different APIs and protocols.
>
> I built four microservices: Traveler, Booking, Integration, and Notification. When a user creates a booking, the Booking Service publishes an event to RabbitMQ. The Integration Service picks it up, calls each supplier — REST/JSON for flights and hotels, SOAP/XML for the car supplier — and publishes the responses back. When all supplier calls succeed, a BookingCompleted event triggers an AWS Lambda that writes an audit record to DynamoDB."

---

## Java 21

**Why:** Modern Java with records, pattern matching, and virtual threads.

**What to mention:**
- Used Java records for all DTOs — immutable, compact, no boilerplate
- Records replace the old POJO + Lombok pattern for simple value objects
- Java 21 is the current LTS (Long-Term Support) version

**Sample answer:**
> "I used Java 21 primarily for records — they're perfect for DTOs because they're immutable by default, and the compiler generates constructors, getters, equals, hashCode automatically. Compare that to a traditional POJO with Lombok: records are more explicit and part of the language spec."

---

## Spring Boot 3.x

**Why:** Industry-standard framework for building production-ready Java services.

**What to mention:**
- Auto-configuration based on classpath — add postgresql dependency → DataSource configured
- Spring Web for REST APIs
- Spring Data JPA for database access
- Spring Validation for input validation
- Spring Actuator for health/metrics
- Spring AMQP for RabbitMQ

**Sample answer:**
> "Spring Boot's auto-configuration is its biggest advantage — I add a dependency, provide properties, and the infrastructure is wired up. For example, adding the PostgreSQL JDBC driver and configuring the datasource URL gives me a fully-configured connection pool. I used constructor injection everywhere rather than field injection — it makes the dependencies explicit and the code easier to unit test."

---

## Service Interface + Implementation Pattern

**Why:** Testability and the Spring AOP proxy model.

**Sample answer:**
> "I define a service interface and inject it into the controller, so the controller depends on an abstraction. This has two practical benefits: first, Spring creates a proxy around the implementation to handle @Transactional — if the controller held a concrete type reference, the proxy wouldn't work correctly. Second, in unit tests I can pass a Mockito mock of the interface to the controller's constructor — no Spring context needed."

---

## PostgreSQL + Flyway

**Why:** Transactional data with versioned schema migrations.

**What to mention:**
- `ddl-auto: validate` — Hibernate checks schema but NEVER modifies it in production
- Flyway runs `V1__create_travelers_table.sql` on startup
- Once a script runs, it's recorded in `flyway_schema_history` and never re-runs
- New schema changes go in `V2__...`, `V3__...` etc.
- UUID primary keys — no auto-increment conflicts in distributed deployment

**Sample answer:**
> "I use Flyway for all schema changes — never `ddl-auto: create-drop`. The rationale is that Hibernate's DDL generation is not safe for production: it can drop and recreate tables. Flyway gives me deterministic, versioned migrations that I can review in code review and that run the same way in test, staging, and production."

---

## JPA / Hibernate

**What to mention:**
- `@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue(UUID)`
- `@OneToMany`, `@ManyToOne` relationships
- `@CreationTimestamp`, `@UpdateTimestamp` for audit fields
- Dirty checking — modified managed entities are saved on commit without explicit `save()`
- `@Transactional(readOnly=true)` on query methods — performance hint to skip dirty checking

**Sample answer:**
> "I use `@Transactional` on all service methods. For read-only queries, I add `readOnly=true` — this is a performance hint that lets Hibernate skip dirty checking (it doesn't need to track changes) and potentially route to a read replica. It's a small but good practice."

---

## RabbitMQ + AMQP

**Why:** Decouple booking creation from supplier calls.

**Architecture:**
- Topic exchange: `travelconnect.events`
- Producer: Booking Service publishes `booking.created`
- Consumer: Integration Service listens on `booking.created.queue`
- Dead letter queue (DLQ): messages that exhaust retries land here for manual inspection

**Sample answer:**
> "Instead of the Booking Service calling suppliers synchronously — which could take several seconds and could fail if a supplier is down — I publish a BookingCreated event to RabbitMQ and return immediately to the user. The Integration Service processes it asynchronously. If a supplier call fails, Spring AMQP retries with exponential backoff. After 3 failures, the message lands in the Dead Letter Queue where it can be inspected and replayed."

---

## Adapter Pattern (Integration Service)

**Why:** Isolate supplier-specific formats from core booking logic.

**Sample answer:**
> "I used the Adapter pattern in the Integration Service. The core booking system works with a canonical `SupplierBookingRequest` model — it has no idea what format any supplier expects. Each adapter (`FlightSupplierAdapter`, `HotelSupplierAdapter`, `CarSupplierAdapter`) translates between the canonical model and the supplier-specific format. Adding a new supplier means adding one adapter class — nothing in the booking logic changes. The adapters are registered in a `SupplierAdapterRegistry` and looked up by supplier type."

---

## SOAP / XML / WSDL / XSD

**Why:** Enterprise travel suppliers — especially legacy ones — use SOAP.

**What to mention:**
- WSDL (Web Services Description Language) — describes the SOAP service contract
- XSD (XML Schema Definition) — defines the structure of XML messages
- The car supplier mock accepts SOAP envelopes and returns SOAP responses
- XML is transformed in `CarSupplierAdapter` — build the envelope, parse the response

**Sample answer:**
> "The car supplier uses SOAP — which is common in enterprise travel (many airline and hotel GDS systems still use it). I defined a WSDL contract and XSD schema for the car booking service. In the adapter, I build a SOAP envelope using Java 21 text blocks, send it as `text/xml` via RestTemplate, and parse the XML response with simple string extraction. In production you'd use JAXB-generated classes from the XSD, but for a portfolio project the manual approach demonstrates the concepts clearly."

---

## AWS Lambda + DynamoDB + CDK

**Why:** Serverless audit log — zero maintenance, pay-per-use.

**What to mention:**
- When a booking completes, Notification Service invokes Lambda
- Lambda writes an immutable record to DynamoDB
- DynamoDB is NoSQL — no schema, pay-per-request billing, auto-scales
- 90-day TTL — records auto-deleted (compliance)
- CDK (TypeScript) — infrastructure as code, type-safe, generates CloudFormation

**Sample answer:**
> "The audit component is deliberately serverless. DynamoDB is ideal because audit records are write-once — there's no relational integrity needed. The Lambda is invoked with the BookingCompleted event and does a DynamoDB PutItem with an idempotency condition (`attribute_not_exists`). This prevents duplicate records if the Lambda is invoked twice for the same booking. I defined the infrastructure using AWS CDK in TypeScript — CDK compiles to CloudFormation templates, so I get type safety in my infrastructure code."

---

## Docker / Docker Compose

**Sample answer:**
> "Every service has a multi-stage Dockerfile. The build stage uses the full JDK to compile the jar. The runtime stage uses only the JRE, making the final image significantly smaller and reducing the attack surface. Docker Compose orchestrates the full local stack with one command: `docker compose up`. Services declare health checks so dependent services wait until dependencies are ready."

---

## Jenkins CI/CD Pipeline

**Stages:** Checkout → Compile → Unit Tests → Integration Tests → SonarQube → Quality Gate → Package → Docker Build

**Sample answer:**
> "The Jenkins pipeline runs in that order deliberately. Unit tests run before integration tests because they're faster — if a unit test fails, you don't waste time starting Docker containers for integration tests. SonarQube runs after tests so it can include coverage data. The Quality Gate step fails the build if the code doesn't meet the minimum quality threshold — this prevents regressions from being merged."

---

## SonarQube

**Sample answer:**
> "SonarQube performs static code analysis — it finds bugs, code smells, and security vulnerabilities that don't cause test failures. I configured it with a Quality Gate that blocks the pipeline if new code introduces critical bugs or drops coverage below a threshold. For a portfolio project this demonstrates that I understand code quality tooling, not just feature development."

---

## Logging + Tracing

**Sample answer:**
> "Every request generates a `traceId` (a UUID) that propagates through RabbitMQ events all the way to the DynamoDB audit record. If a booking fails or behaves unexpectedly, I grep the logs across all services for that traceId and see the complete picture. The log format includes the service name, thread, and class on every line — this is essential when you're looking at interleaved logs from multiple services."

---

## Performance Troubleshooting

**How to investigate a slow request:**
1. Check Actuator metrics: `/actuator/metrics/http.server.requests` — find which endpoint is slow
2. Check HikariCP pool: `/actuator/metrics/hikaricp.connections.active` — pool exhaustion?
3. Thread dump: `/actuator/threaddump` — find blocked threads
4. Heap dump: `/actuator/heapdump` — find memory leaks (analyse with VisualVM/Eclipse MAT)
5. JVM GC metrics: `/actuator/metrics/jvm.gc.pause` — excessive GC?

**Sample answer:**
> "I exposed the standard Actuator endpoints including threaddump and heapdump. For a controlled demo, the Notification Service has a `/perf-test` endpoint that triggers a bounded CPU workload. You can watch the JVM metrics climb in Actuator while it runs, take a thread dump and see the thread in RUNNABLE state, confirming it's CPU-bound rather than blocked on I/O. This demonstrates the diagnostic workflow without any actual production issues."

---

## Testing Strategy

**Unit tests:** `@ExtendWith(MockitoExtension.class)` — no Spring context, no DB. Fast. Test business logic.

**Controller slice tests:** `@WebMvcTest` — starts only the web layer, mocks the service. Tests HTTP serialization, validation, status codes.

**Integration tests:** `@SpringBootTest` + Testcontainers — starts a real PostgreSQL Docker container, runs Flyway migrations, tests the full stack. Catches SQL errors that H2 wouldn't catch.

**Sample answer:**
> "I have three layers of tests. Unit tests are the fastest and test pure business logic — service methods with mocked repositories. Controller tests use @WebMvcTest which spins up only the MVC layer, so I can test request/response handling without a database. Integration tests use Testcontainers to start a real PostgreSQL container — this is important because H2's SQL dialect differs from PostgreSQL's, so integration tests can catch bugs that unit tests miss."
