# TravelConnect — Troubleshooting Guide

## How to Inspect Logs

### Viewing service logs (Docker)

```bash
# Follow live logs for a service
docker logs -f travelconnect-booking

# Last 100 lines
docker logs --tail 100 travelconnect-integration

# All services simultaneously
docker compose -f docker/docker-compose.yml logs -f
```

### Searching logs for a specific booking

Every booking carries a `traceId` that propagates across all services and queues. Use it to correlate logs:

```bash
# Find all log lines for a specific traceId across all services
docker compose -f docker/docker-compose.yml logs | grep "abc-123-trace-id"

# Find all log lines for a booking reference
docker compose -f docker/docker-compose.yml logs | grep "TC-AB12CD34"
```

### Log format

Every line follows this pattern:
```
2024-01-15 10:30:00.123 [http-nio-8082-exec-1] INFO  [booking-service] c.t.b.s.impl.BookingServiceImpl - Creating booking for travelerId=...
```

Fields:
- `[thread-name]` — which thread handled the request
- `[service-name]` — which microservice
- `logger` — class that emitted the log
- `traceId=...` — appears on all booking-related logs

---

## How to Trace a Booking End-to-End

1. **Create a booking** — note the `bookingId` and `traceId` from the response
2. **Check booking status**: `GET /api/v1/bookings/{id}/status`
3. **Check integration requests**: `GET /api/v1/integrations/booking/{bookingId}` (Integration Service)
4. **Search logs**: `grep "traceId=<value>"` across all service logs

---

## How to Identify Failed Supplier Integrations

### Via API
```bash
# Get all integration requests for a booking
curl http://localhost:8083/api/v1/integrations/booking/{bookingId}

# Check supplier availability
curl http://localhost:8083/api/v1/integrations/suppliers
```

### Via logs
Look for:
```
ERROR [integration-service] ... Car supplier call failed: ...
WARN  [integration-service] ... Supplier CAR-SUPPLIER-01 unavailable after 3 attempts
```

### Via RabbitMQ Management UI
1. Open `http://localhost:15672` (login: `travelconnect` / `travelconnect`)
2. Navigate to **Queues**
3. Check `booking.created.queue.dlq` — any messages here are failed bookings that exhausted all retries
4. You can inspect the message payload and manually republish if needed

---

## How to Investigate Slow Requests

### Step 1: Identify the slow endpoint

Check Actuator metrics for request latencies:
```bash
# All HTTP request metrics
curl http://localhost:8082/actuator/metrics/http.server.requests

# Filter by endpoint
curl "http://localhost:8082/actuator/metrics/http.server.requests?tag=uri:/api/v1/bookings"
```

### Step 2: Check database connection pool

```bash
# HikariCP pool metrics
curl http://localhost:8082/actuator/metrics/hikaricp.connections.active
curl http://localhost:8082/actuator/metrics/hikaricp.connections.pending
```

If `pending > 0`, you have more requests than database connections. Increase `hikari.maximum-pool-size` or optimize slow queries.

### Step 3: Check JVM heap

```bash
# Current heap usage
curl http://localhost:8082/actuator/metrics/jvm.memory.used

# GC pause time
curl http://localhost:8082/actuator/metrics/jvm.gc.pause
```

High GC activity causes latency spikes. Consider increasing heap via `JAVA_OPTS=-Xmx512m`.

### Step 4: Take a thread dump (to find blocked threads)

```bash
# Via Actuator
curl http://localhost:8082/actuator/threaddump

# Or via JVM (get the PID first)
jstack <pid>
```

Look for threads in `BLOCKED` or `WAITING` state — these indicate lock contention or slow I/O.

### Step 5: Take a heap dump (to find memory leaks)

```bash
# Via Actuator — downloads a .hprof file
curl http://localhost:8082/actuator/heapdump -o heap.hprof

# Analyse with VisualVM or Eclipse MAT
```

---

## Performance Test Demo

The Notification Service exposes a controlled CPU workload endpoint for educational investigation:

```bash
# Trigger the workload
curl -X POST "http://localhost:8084/api/v1/admin/notifications/perf-test?iterations=5000000"

# In another terminal — watch the JVM metrics during the test
watch -n 1 'curl -s http://localhost:8084/actuator/metrics/jvm.memory.used | python3 -m json.tool'

# Take a thread dump during the test
curl http://localhost:8084/actuator/threaddump | python3 -m json.tool
```

**What to look for:**
- Thread dump shows `perf-test-thread` in `RUNNABLE` state → confirms it's CPU bound, not blocked
- JVM heap stays relatively flat → the workload is CPU intensive, not memory intensive
- `jvm.gc.pause` increases slightly under load

---

## Common Errors and Fixes

| Error | Cause | Fix |
|---|---|---|
| `Connection refused` to PostgreSQL | Docker infra not running | `docker compose -f docker/docker-compose.infra.yml up -d` |
| `Connection refused` to RabbitMQ | Docker infra not running | Same as above |
| `FlywayException: Validate failed` | Entity changed without migration | Create a new `V{n}__description.sql` migration |
| `DataIntegrityViolationException` for email | Race condition on duplicate | Usually swallowed by service-layer check — investigate if it reaches the API |
| `AmqpConnectException` | RabbitMQ not ready when service starts | Wait for health check — use `depends_on: condition: service_healthy` in Docker Compose |
| `MethodArgumentNotValidException` | Invalid request body | Check the `data` field in the 400 response for which fields failed |
| Booking stuck in `PROCESSING` | Supplier didn't respond | Check Integration Service logs for that `traceId`; check DLQ |
| Lambda not being called | `aws.enabled=false` | Normal for local development — set `AWS_ENABLED=true` with real AWS credentials |

---

## RabbitMQ Dead Letter Queue (DLQ) Inspection

```bash
# List messages in DLQ without consuming them (Management API)
curl -u travelconnect:travelconnect \
  "http://localhost:15672/api/queues/%2F/booking.created.queue.dlq"

# Get a message from DLQ for inspection
curl -u travelconnect:travelconnect \
  -X POST "http://localhost:15672/api/queues/%2F/booking.created.queue.dlq/get" \
  -H "Content-Type: application/json" \
  -d '{"count": 1, "requeue": true, "encoding": "auto"}'
```

To replay a message from the DLQ:
1. Get the message payload
2. Fix the underlying issue (e.g., restart the supplier)
3. Re-publish the message to `travelconnect.events` exchange with routing key `booking.created`

---

## Health Endpoints Reference

| URL | What it checks |
|---|---|
| `:8081/actuator/health` | Traveler Service (DB connection) |
| `:8082/actuator/health` | Booking Service (DB + RabbitMQ) |
| `:8083/actuator/health` | Integration Service (DB + RabbitMQ + supplier availability) |
| `:8084/actuator/health` | Notification Service (RabbitMQ) |
| `:15672` | RabbitMQ Management UI |
