# ⚠️ Exception Handling

> **Module:** Global Exception Handling  
> **Framework:** Spring Boot  
> **Annotation:** `@RestControllerAdvice`

---

# 📖 Overview

No matter how well an application is designed, errors are unavoidable.

Examples:

- User enters an incorrect password.
- A requested resource does not exist.
- Validation fails.
- An event conflicts with another event.
- A hall is already booked.

Instead of allowing these errors to crash the application or return confusing stack traces, EventFlow converts them into **structured JSON responses** that are easy for clients to understand.

This is achieved using **Global Exception Handling**.

---

# Why Do We Need Exception Handling?

Imagine a registration request.

```
POST /api/users/register
```

A user tries to register with an email that already exists.

Without exception handling:

```
Application

↓

Throws Exception

↓

500 Internal Server Error

↓

Stack Trace
```

The client receives an unexpected server error even though the mistake was made by the user.

This is a poor user experience.

Instead, we return:

```json
{
    "status":409,
    "error":"Conflict",
    "message":"User already exists"
}
```

This clearly explains what went wrong.

---

# Traditional Approach

A beginner might write:

```java
@PostMapping
public ResponseEntity<?> register(...) {

    try {

        ...

    } catch(Exception ex) {

        ...

    }

}
```

Every controller would contain:

- try
- catch
- Response building

Eventually every controller becomes repetitive.

Example:

```
UserController

↓

try

↓

catch
```

```
EventController

↓

try

↓

catch
```

```
VenueController

↓

try

↓

catch
```

The project quickly becomes difficult to maintain.

---

# Global Exception Handling

Spring Boot provides:

```java
@RestControllerAdvice
```

Instead of handling exceptions in every controller,

Spring intercepts exceptions globally.

```
Controller

↓

Exception

↓

GlobalExceptionHandler

↓

JSON Response
```

Controllers remain clean.

---

# GlobalExceptionHandler

This class is responsible for converting Java Exceptions into HTTP responses.

Responsibilities:

- Catch exceptions
- Select HTTP Status
- Build ErrorResponse
- Return JSON

Every controller automatically benefits from this handler.

---

# ErrorResponse

Most exceptions return a common response object.

Example:

```json
{
    "timestamp":"2026-08-05T10:15:20",
    "status":404,
    "error":"Not Found",
    "message":"Hall not found"
}
```

Fields:

| Field | Purpose |
|--------|----------|
| timestamp | When the error occurred |
| status | HTTP Status Code |
| error | Standard HTTP Status |
| message | Human-readable description |

This keeps every error response consistent.

---

# ValidationErrorResponse

Validation errors are slightly different.

Example:

```json
{
    "timestamp":"...",
    "status":400,
    "error":"Bad Request",
    "message":"Validation failed",
    "fieldErrors":{

        "name":"must not be blank",

        "city":"must not be blank",

        "latitude":"must not be null"

    }
}
```

Instead of one message,

the client receives all invalid fields.

This greatly improves the user experience.

---

# Current Exception Categories

EventFlow currently uses multiple categories of exceptions.

```
Authentication

↓

Authorization

↓

User

↓

Hall

↓

Event

↓

Validation
```

Grouping exceptions by domain makes the project easier to navigate.

---

# Authentication Exceptions

Authentication answers:

> Who are you?

Examples:

- Wrong password
- Invalid JWT
- Expired JWT
- Missing JWT

Current exception:

```
InvalidCredentialsException
```

HTTP Response:

```
401 Unauthorized
```

Example:

```json
{
    "status":401,
    "message":"Invalid email or password"
}
```

---

# Authorization Exceptions

Authorization answers:

> Are you allowed to perform this action?

Example:

```
USER

↓

POST /api/admin/venues
```

Result:

```
403 Forbidden
```

Handled by:

```
CustomAccessDeniedHandler
```

Example:

```json
{
    "status":403,
    "message":"Access denied"
}
```

---

# User Exceptions

Current exceptions:

```
UserAlreadyExistsException

UserNotFoundException
```

Examples:

Trying to register twice:

```
409 Conflict
```

Searching for a missing user:

```
404 Not Found
```

---

# Hall Exceptions

Current exceptions:

```
HallNotFoundException

HallAlreadyBookedException
```

Examples:

```
Hall ID does not exist

↓

404
```

```
Hall already reserved

↓

409
```

---

# Event Exceptions

Current exception:

```
InvalidEventTimeException
```

Example:

```
Start Time

↓

10 PM

End Time

↓

8 PM
```

This is invalid.

Response:

```
400 Bad Request
```

---

# Validation Exceptions

Validation occurs before business logic.

Example:

```
Client

↓

CreateVenueRequest

↓

@NotBlank

↓

Controller

↓

MethodArgumentNotValidException
```

The request never reaches the Service.

Example response:

```json
{
    "message":"Validation failed",
    "fieldErrors":{
        "name":"must not be blank"
    }
}
```

---

# Exception Flow

Suppose an organizer creates an event.

```
Controller

↓

EventService

↓

Hall Already Booked?

↓

YES

↓

Throw HallAlreadyBookedException

↓

GlobalExceptionHandler

↓

409 Conflict

↓

JSON Response
```

Notice that the Controller never catches the exception.

Spring handles everything automatically.

---

# HTTP Status Codes Used

| Status | Meaning | Used For |
|----------|----------|-----------|
| 200 OK | Successful request | GET, Login |
| 201 Created | Resource created | Venue, Event |
| 400 Bad Request | Invalid request | Validation, Invalid Event Time |
| 401 Unauthorized | Authentication failed | Invalid Login, Missing JWT |
| 403 Forbidden | Permission denied | User accessing Admin APIs |
| 404 Not Found | Resource missing | User, Hall |
| 409 Conflict | Business conflict | Duplicate User, Hall Already Booked |
| 500 Internal Server Error | Unexpected server error | Unhandled Exceptions |

Choosing the correct HTTP status code makes APIs easier to understand and integrate.

---

# Why Custom Exceptions?

Instead of throwing:

```java
throw new RuntimeException("...");
```

EventFlow uses custom exceptions.

Example:

```java
throw new HallAlreadyBookedException(
    "Hall is already booked."
);
```

Advantages:

- Better readability
- Easier debugging
- Domain-specific meaning
- Easier to handle individually

When reading the code,

```
HallAlreadyBookedException
```

is much more descriptive than

```
RuntimeException
```

---

# Advantages of Global Exception Handling

- Controllers remain clean.
- One place to manage errors.
- Consistent JSON responses.
- Easier maintenance.
- Easier debugging.
- Better API design.
- Better frontend integration.

---

# Future Improvements

As EventFlow grows, more exceptions will be introduced.

Examples:

```
VenueNotFoundException

SeatAlreadyBookedException

BookingNotFoundException

PaymentFailedException

NotificationFailedException
```

Eventually, a generic fallback handler can also be added:

```java
@ExceptionHandler(Exception.class)
```

to prevent unexpected exceptions from exposing internal implementation details.

---

# Summary

EventFlow centralizes exception handling using `@RestControllerAdvice`.

Instead of handling errors inside every controller, exceptions are thrown from the Service layer and automatically converted into meaningful HTTP responses.

This approach keeps controllers clean, improves maintainability, and provides clients with consistent, structured, and informative error messages.