# TravelConnect — API Reference

All endpoints return an `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

Error responses:
```json
{
  "success": false,
  "message": "Traveler not found with id: abc-123",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Traveler Service — `http://localhost:8081`

### Create Traveler
```
POST /api/v1/travelers
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phone": "+441234567890",
  "dateOfBirth": "1990-05-15",
  "nationality": "British",
  "passportNumber": "AB123456",
  "passportExpiry": "2030-05-15"
}
```
Response: `201 Created`
```json
{
  "success": true,
  "message": "Traveler created successfully",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### Get Traveler
```
GET /api/v1/travelers/{id}
```

### List Travelers (paginated)
```
GET /api/v1/travelers?page=0&size=20&lastName=smith
```

### Update Traveler
```
PUT /api/v1/travelers/{id}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith-Jones",
  "phone": "+441234567891",
  "nationality": "British",
  "passportNumber": "CD789012",
  "passportExpiry": "2035-05-15"
}
```

### Delete Traveler
```
DELETE /api/v1/travelers/{id}
```
Response: `204 No Content`

---

## Booking Service — `http://localhost:8082`

### Search Travel
```
POST /api/v1/search
Content-Type: application/json

{
  "origin": "LHR",
  "destination": "JFK",
  "departureDate": "2025-03-15",
  "returnDate": "2025-03-22",
  "passengers": 1,
  "includeFlights": true,
  "includeHotels": true,
  "includeCars": false
}
```
Response: `200 OK` with flights, hotels, cars arrays

### Create Trip
```
POST /api/v1/trips
Content-Type: application/json

{
  "travelerId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Q1 Sales Conference",
  "destination": "New York, USA",
  "startDate": "2025-03-15",
  "endDate": "2025-03-22",
  "description": "Annual Q1 sales kick-off"
}
```
Response: `201 Created`

### Get Trip
```
GET /api/v1/trips/{id}
```

### Get Trips for Traveler
```
GET /api/v1/trips?travelerId={id}&page=0&size=20
```

### Create Booking
```
POST /api/v1/bookings
Content-Type: application/json

{
  "tripId": "trip-uuid-here",
  "travelerId": "traveler-uuid-here",
  "currency": "GBP",
  "items": [
    {
      "itemType": "FLIGHT",
      "supplierCode": "FL-LHR-JFK-001",
      "origin": "LHR",
      "destination": "JFK",
      "departureDate": "2025-03-15",
      "returnDate": "2025-03-22",
      "passengers": 1,
      "pricePerUnit": 299.99,
      "quantity": 1,
      "currency": "GBP"
    },
    {
      "itemType": "HOTEL",
      "supplierCode": "HL-NYC-HILTON",
      "origin": "JFK",
      "destination": "NYC",
      "departureDate": "2025-03-15",
      "returnDate": "2025-03-22",
      "passengers": 1,
      "pricePerUnit": 149.99,
      "quantity": 7,
      "currency": "GBP"
    }
  ]
}
```
Response: `201 Created`
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": "booking-uuid",
    "bookingReference": "TC-AB12CD34",
    "status": "PENDING",
    "totalAmount": 1349.92,
    "currency": "GBP",
    "traceId": "trace-uuid-for-log-correlation"
  }
}
```

### Get Booking
```
GET /api/v1/bookings/{id}
```

### Get Booking Status
```
GET /api/v1/bookings/{id}/status
```

### Get Bookings for Traveler
```
GET /api/v1/bookings?travelerId={id}&page=0&size=20
```

### Get Booking by Reference
```
GET /api/v1/bookings/reference/TC-AB12CD34
```

---

## Integration Service — `http://localhost:8083`

### Get Integration Request
```
GET /api/v1/integrations/{id}
```

### Get All Integrations for a Booking
```
GET /api/v1/integrations/booking/{bookingId}
```
Response includes all supplier calls (FLIGHT, HOTEL, CAR) with their status and retry count.

### Get Supplier Status
```
GET /api/v1/integrations/suppliers
```
Response:
```json
{
  "success": true,
  "data": [
    { "supplierId": "FLIGHT-SUPPLIER-01", "supplierType": "FLIGHT", "available": true },
    { "supplierId": "HOTEL-SUPPLIER-01", "supplierType": "HOTEL", "available": true },
    { "supplierId": "CAR-SUPPLIER-01", "supplierType": "CAR", "available": true }
  ]
}
```

---

## Health Endpoints (all services)

```
GET /actuator/health
GET /actuator/metrics
GET /actuator/metrics/jvm.memory.used
GET /actuator/metrics/http.server.requests
GET /actuator/threaddump
GET /actuator/heapdump
GET /actuator/env
```

---

## RabbitMQ Management
```
http://localhost:15672
Username: travelconnect
Password: travelconnect
```

---

## HTTP Status Codes Used

| Code | Meaning |
|---|---|
| 200 OK | Successful GET / PUT |
| 201 Created | Successful POST |
| 204 No Content | Successful DELETE |
| 400 Bad Request | Validation failure |
| 404 Not Found | Resource doesn't exist |
| 409 Conflict | Duplicate resource (e.g. email) |
| 500 Internal Server Error | Unexpected server error |
| 503 Service Unavailable | Supplier unavailable (mock suppliers only) |
