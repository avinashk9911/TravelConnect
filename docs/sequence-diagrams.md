# TravelConnect — Sequence Diagrams

## 1. Creating a Booking

This is the core flow. The key insight is that the API returns immediately after saving the booking — it does NOT wait for supplier responses.

```mermaid
sequenceDiagram
    actor Client
    participant BS as Booking Service :8082
    participant DB as PostgreSQL
    participant RMQ as RabbitMQ
    participant IS as Integration Service :8083

    Client->>BS: POST /api/v1/bookings { tripId, items... }
    BS->>BS: Validate request (@Valid)
    BS->>DB: Verify tripId exists
    DB-->>BS: Trip found
    BS->>BS: Generate bookingReference (TC-XXXXXXXX)
    BS->>DB: INSERT booking (status=PENDING)
    BS->>DB: INSERT booking_items
    DB-->>BS: Saved (bookingId, reference)
    BS->>RMQ: Publish BookingCreatedEvent (routing: booking.created)
    RMQ-->>BS: ACK
    BS-->>Client: 201 Created { bookingId, bookingReference: "TC-AB123456", status: "PENDING" }

    Note over BS,RMQ: API response in < 100ms regardless of supplier speed

    RMQ->>IS: BookingCreatedEvent (async)
    IS->>IS: For each item: create IntegrationRequest
    IS->>IS: Call supplier adapter
```

## 2. Supplier Integration Processing

After the booking is created, the Integration Service processes each item asynchronously.

```mermaid
sequenceDiagram
    participant IS as Integration Service
    participant DB as PostgreSQL
    participant RMQ as RabbitMQ
    participant BS as Booking Service
    participant FS as Flight Supplier :9001
    participant HS as Hotel Supplier :9002
    participant CS as Car Supplier :9003

    Note over IS: Processing BookingCreatedEvent with 3 items

    par Flight booking
        IS->>DB: INSERT integration_request (FLIGHT, PENDING)
        IS->>FS: POST /api/bookings (JSON)
        FS-->>IS: { referenceId: "FL-abc", status: "CONFIRMED", price: 299.99 }
        IS->>DB: INSERT integration_response (success=true)
        IS->>DB: UPDATE integration_request (SUCCESS)
        IS->>RMQ: Publish SupplierResponseReceived (routing: supplier.response.flight)
    and Hotel booking
        IS->>DB: INSERT integration_request (HOTEL, PENDING)
        IS->>HS: POST /api/bookings (JSON)
        HS-->>IS: { referenceId: "HL-xyz", status: "CONFIRMED", price: 149.99 }
        IS->>DB: INSERT integration_response (success=true)
        IS->>DB: UPDATE integration_request (SUCCESS)
        IS->>RMQ: Publish SupplierResponseReceived (routing: supplier.response.hotel)
    and Car booking (SOAP)
        IS->>DB: INSERT integration_request (CAR, PENDING)
        IS->>CS: SOAP POST /ws/car-booking (XML)
        CS-->>IS: SOAP response (XML)
        IS->>IS: Parse SOAP XML response
        IS->>DB: INSERT integration_response
        IS->>DB: UPDATE integration_request
        IS->>RMQ: Publish SupplierResponseReceived
    end

    RMQ->>BS: SupplierResponseReceived (x3)
    BS->>BS: When all responses received → status=CONFIRMED
    BS->>DB: UPDATE booking (status=CONFIRMED)
    BS->>RMQ: Publish BookingCompletedEvent
```

## 3. Booking Completion + AWS Lambda Audit

```mermaid
sequenceDiagram
    participant BS as Booking Service
    participant RMQ as RabbitMQ
    participant NS as Notification Service :8084
    participant Lambda as AWS Lambda
    participant DDB as DynamoDB

    BS->>RMQ: BookingCompletedEvent { bookingId, travelerId, bookingReference, totalAmount }
    RMQ->>NS: BookingCompletedEvent (booking.completed queue)

    NS->>NS: Log event receipt
    NS->>NS: Simulate email notification
    NS->>Lambda: Invoke travelconnect-booking-audit { bookingId, travelerId, ... }

    Lambda->>Lambda: Build audit record
    Lambda->>DDB: PutItem (ConditionExpression: attribute_not_exists)
    DDB-->>Lambda: Success (or ConditionalCheckFailed if duplicate → idempotent)
    Lambda-->>NS: { statusCode: 200, bookingId }
    NS->>NS: Log audit confirmed

    Note over DDB: Record with 90-day TTL auto-deleted
```

## 4. Failed Supplier + Retry + Dead Letter Queue

```mermaid
sequenceDiagram
    participant IS as Integration Service
    participant CS as Car Supplier :9003
    participant RMQ as RabbitMQ
    participant DLQ as Dead Letter Queue

    IS->>CS: SOAP request (attempt 1)
    CS-->>IS: 503 Service Unavailable

    Note over IS: Spring AMQP retry configured:<br/>initial-interval: 2000ms<br/>max-attempts: 3<br/>multiplier: 2.0

    IS->>CS: SOAP request (attempt 2, after 2s)
    CS-->>IS: 503 Service Unavailable

    IS->>CS: SOAP request (attempt 3, after 4s)
    CS-->>IS: 503 Service Unavailable

    IS->>IS: Max retries exceeded
    IS->>IS: Log error: "Supplier CAR-SUPPLIER-01 unavailable after 3 attempts"
    IS->>RMQ: NACK message (not requeued)
    RMQ->>DLQ: Move message to booking.created.queue.dlq

    Note over DLQ: Message preserved for manual inspection<br/>or later replay
    Note over IS: Booking status: FAILED
    Note over IS: IntegrationRequest: status=FAILED, retryCount=3
```

## 5. Validation Error Flow

```mermaid
sequenceDiagram
    actor Client
    participant TC as TravelerController
    participant GEH as GlobalExceptionHandler

    Client->>TC: POST /api/v1/travelers { email: "not-an-email", firstName: "" }
    TC->>TC: @Valid — Bean Validation fails
    TC->>GEH: MethodArgumentNotValidException
    GEH->>GEH: Collect all field errors
    GEH-->>Client: HTTP 400 { success: false, message: "Validation failed", data: { email: "...", firstName: "..." } }

    Note over Client,GEH: Caller knows exactly which fields are wrong<br/>No stack trace exposed
```
