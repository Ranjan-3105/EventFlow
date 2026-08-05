# 17. Event Module

# Overview

The Event Module is responsible for allowing organizers to schedule events inside available halls.

This is the first module in EventFlow that implements actual business logic beyond CRUD operations.

Unlike creating users or venues, creating an event requires multiple validations before data can be saved.

---

# Responsibilities

The Event Module is responsible for

- Creating events
- Assigning events to halls
- Associating events with organizers
- Preventing scheduling conflicts
- Managing event status

Future responsibilities

- Update events
- Cancel events
- Publish events
- Archive events

---

# Business Problem

Consider a cinema hall.

```
Hall 1
```

An organizer schedules

```
10:00

↓

13:00
```

Another organizer requests

```
11:00

↓

14:00
```

Both events cannot use the same hall simultaneously.

The Event Module prevents this situation.

---

# Event Entity

Current structure

```
Event

id

title

description

bannerUrl

startTime

endTime

status

hall

organizer

createdAt

updatedAt
```

Relationships

```
Organizer

↓

Many Events
```

```
Hall

↓

Many Events
```

An event belongs to exactly one organizer and one hall.

---

# Event Status

Current implementation

```
DRAFT
```

Future versions may include

```
DRAFT

PUBLISHED

CANCELLED

COMPLETED
```

Using an enum makes the state explicit and type-safe.

---

# Request DTO

```
CreateEventRequest
```

Purpose

Receives data from the client.

Example

```json
{
    "title":"Spring Boot Workshop",
    "description":"Hands-on backend workshop",
    "bannerUrl":"https://example.com/banner.jpg",
    "hallId":"4d415308-9798-46be-b47d-f27f6c39b32a",
    "startTime":"2026-08-20T10:00:00Z",
    "endTime":"2026-08-20T13:00:00Z"
}
```

DTOs ensure that only required fields are accepted from clients.

---

# Response DTO

```
EventResponse
```

Current response

```json
{
    "id":"e6dcfed6-f92c-498e-aa0b-11c0e905d50c",
    "title":"Spring Boot Workshop",
    "startTime":"2026-08-20T10:00:00Z",
    "endTime":"2026-08-20T13:00:00Z",
    "status":"DRAFT"
}
```

Returning DTOs instead of entities keeps the API stable and prevents exposing internal implementation details.

---

# Controller

Endpoint

```
POST /api/events
```

Responsibilities

- Receive HTTP request
- Validate request
- Call Service
- Return HTTP response

The controller contains no business logic.

---

# Service Layer

The Service layer contains all business rules.

Workflow

```
Receive Request

↓

Get Logged-in Organizer

↓

Find Hall

↓

Validate Time

↓

Check Hall Availability

↓

Create Event

↓

Save

↓

Return DTO
```

---

# Authentication

The organizer is identified using Spring Security.

```
SecurityContextHolder

↓

Authentication

↓

Email

↓

User Repository

↓

Organizer
```

The client never sends the organizer ID.

It is derived from the authenticated user.

---

# Business Validations

Before creating an event,

the following checks are performed.

---

## Organizer Exists

```
UserRepository

↓

findByEmail(...)
```

If missing

```
UserNotFoundException
```

---

## Hall Exists

```
HallRepository

↓

findById(...)
```

If missing

```
HallNotFoundException
```

---

## Time Validation

Business Rule

```
Start Time

↓

End Time
```

If

```
Start >= End
```

Result

```
InvalidEventTimeException
```

---

## Hall Availability

Most important validation.

The repository executes

```
existsConflictingEvent(...)
```

If

```
true
```

↓

```
HallAlreadyBookedException
```

---

# Conflict Detection

Existing Event

```
10:00

↓

13:00
```

New Event

```
11:00

↓

14:00
```

Result

```
Conflict
```

The repository uses JPQL.

```java
SELECT COUNT(e) > 0
FROM Event e
...
```

This checks whether any event already occupies the requested interval.

---

# Why JPQL?

Query Derivation would produce an unreadable method.

Example

```
existsByHallIdAndStartTimeBeforeAndEndTimeAfter(...)
```

The business logic becomes difficult to understand.

JPQL expresses the overlap rule much more clearly.

---

# Event Creation

After all validations pass

```
Create Event

↓

Assign Hall

↓

Assign Organizer

↓

Assign Status

↓

Save
```

The event is persisted.

---

# Exception Handling

Possible exceptions

```
UserNotFoundException

HallNotFoundException

InvalidEventTimeException

HallAlreadyBookedException
```

Mapped HTTP responses

```
404

404

400

409
```

---

# Request Lifecycle

```
Client

↓

Controller

↓

Validation

↓

Service

↓

Business Rules

↓

Repository

↓

PostgreSQL

↓

Repository

↓

Service

↓

Controller

↓

Client
```

---

# Why This Architecture?

Responsibilities are separated.

Controller

↓

HTTP

Service

↓

Business Logic

Repository

↓

Database

This separation keeps the application easy to understand and maintain.

---

# Current Limitations

Current implementation

- Event creation only
- Draft status only
- Single hall assignment

Upcoming improvements

- Update event
- Publish event
- Cancel event
- Event search
- Pagination
- Event categories

---

# Production Perspective

Large event platforms follow a similar workflow.

Organizer

↓

Requests Hall

↓

Availability Check

↓

Reserve Hall

↓

Publish Event

The Event Module models this real-world process while enforcing business rules before any data reaches the database.

---

# Summary

The Event Module is the first feature in EventFlow that combines

- Authentication
- Authorization
- DTOs
- Validation
- Business Rules
- JPQL
- Exception Handling
- JPA Relationships

into a complete workflow.

It demonstrates how multiple Spring Boot components work together to implement a real production use case rather than a simple CRUD operation.