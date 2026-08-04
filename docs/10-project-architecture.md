# 🏗️ Project Architecture

> **Architecture Style:** Layered Architecture (Controller → Service → Repository)  
> **Framework:** Spring Boot  
> **Database:** PostgreSQL  
> **ORM:** Spring Data JPA (Hibernate)

---

# 📖 Overview

EventFlow follows a **Layered Architecture**, one of the most widely used backend architectures in enterprise Java applications.

Instead of placing all logic inside controllers, responsibilities are divided into multiple layers.

Each layer has only **one responsibility** and communicates only with the layer directly below it.

```
Client

↓

Controller

↓

Service

↓

Repository

↓

Database
```

This makes the project:

- Easy to maintain
- Easy to test
- Easier to debug
- Easier to scale
- Easier for multiple developers to work on simultaneously

---

# Why Layered Architecture?

Imagine writing everything inside one controller.

```java
@PostMapping("/register")
public ResponseEntity<?> register(...) {

    // Validation

    // Business Logic

    // Database Query

    // JWT Generation

    // Response Mapping

}
```

The controller quickly becomes hundreds of lines long.

Now imagine a project with:

- Users
- Events
- Bookings
- Seats
- Payments

The code would become impossible to maintain.

Instead, we separate concerns.

---

# Overall Architecture

```
                HTTP Request

                     │
                     ▼

              Controller Layer

                     │
                     ▼

               Service Layer

                     │
                     ▼

             Repository Layer

                     │
                     ▼

                PostgreSQL
```

The response travels back in the opposite direction.

---

# Request Lifecycle

Whenever a client sends a request, it follows this path.

```
Client

↓

Controller

↓

DTO Validation

↓

Service

↓

Repository

↓

Database

↓

Repository

↓

Service

↓

Response DTO

↓

Controller

↓

Client
```

Understanding this lifecycle is one of the most important concepts in backend development.

---

# Project Structure

```
src/main/java
│
├── config/
│
├── controller/
│
├── dto/
│   ├── request/
│   └── response/
│
├── entity/
│
├── exception/
│
├── repository/
│
├── security/
│
├── service/
│
└── EventflowApplication.java
```

Each package has a dedicated responsibility.

---

# Controller Layer

```
controller/
```

Controllers are responsible for handling HTTP requests.

Responsibilities:

- Receive HTTP Requests
- Validate Request DTOs
- Call Services
- Return HTTP Responses

Controllers **must not** contain business logic.

Example:

```
POST /api/events

↓

EventController

↓

EventService
```

The controller simply delegates the work.

---

# Service Layer

```
service/
```

This is the heart of the application.

Services contain the business logic.

Responsibilities:

- Apply business rules
- Coordinate multiple repositories
- Validate business conditions
- Create entities
- Update entities
- Delete entities
- Generate responses

Example:

Creating an Event requires:

```
Find Organizer

↓

Find Hall

↓

Check Hall Availability

↓

Create Event

↓

Save Event
```

Notice how multiple repositories work together.

That coordination belongs inside the Service layer.

---

# Repository Layer

```
repository/
```

Repositories are responsible for communicating with the database.

Responsibilities:

- Save Entities
- Find Entities
- Delete Entities
- Execute Queries

Repositories should **never** contain business logic.

Example:

```
EventRepository

↓

existsConflictingEvent(...)
```

The repository answers:

> "Does a conflicting event exist?"

It does **not** decide what to do if one exists.

That decision belongs to the Service.

---

# Entity Layer

```
entity/
```

Entities represent database tables.

Example:

```
User

↓

users table
```

```
Venue

↓

venues table
```

```
Hall

↓

hall table
```

```
Event

↓

events table
```

Entities contain:

- Fields
- Relationships
- JPA Annotations

They should **not** contain business logic.

---

# DTO Layer

DTO stands for:

> **Data Transfer Object**

DTOs are used to transfer data between the client and the server.

EventFlow separates DTOs into two categories.

```
request/

response/
```

---

# Request DTO

Example:

```
CreateEventRequest
```

Contains only the data sent by the client.

```
title

description

hallId

startTime

endTime
```

Notice that it does **not** contain:

```
organizerId
```

because the organizer comes from JWT authentication.

---

# Response DTO

Example:

```
EventResponse
```

Contains only the information returned to the client.

Example:

```
id

title

status

startTime

endTime
```

Response DTOs prevent exposing internal entity details such as:

- Passwords
- Hibernate proxies
- Lazy-loaded relationships
- Internal fields

---

# Why Not Return Entities?

Suppose we returned the User entity directly.

```
User

↓

password

email

role

events

bookings

...
```

The client would receive much more information than necessary.

DTOs solve this problem by exposing only the required fields.

---

# Dependency Injection

Spring automatically creates and manages objects.

Instead of writing:

```java
UserRepository repository = new UserRepository();
```

Spring injects it automatically.

Example:

```java
private final UserRepository userRepository;

public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

Advantages:

- Loose coupling
- Easier testing
- Better maintainability

---

# Business Logic vs Database Logic

One of the biggest design principles in EventFlow is:

> Business logic belongs in Services.

Example:

Repository:

```
Does Hall conflict exist?
```

Service:

```
If conflict exists

↓

Throw HallAlreadyBookedException
```

The repository answers questions.

The service makes decisions.

---

# Validation

Validation happens before business logic.

Example:

```
Client

↓

CreateEventRequest

↓

@NotBlank

@NotNull

↓

Controller

↓

Service
```

Invalid requests never reach the business logic.

---

# Exception Handling

Instead of writing try-catch blocks inside every controller,

EventFlow uses:

```
GlobalExceptionHandler
```

Responsibilities:

- Convert Exceptions into JSON
- Return correct HTTP Status
- Keep controllers clean

Example:

```
UserAlreadyExistsException

↓

409 Conflict
```

```
InvalidCredentialsException

↓

401 Unauthorized
```

---

# Security Integration

Spring Security executes before the Controller.

```
Request

↓

JWT Filter

↓

SecurityContext

↓

Controller
```

This means the Controller and Service already know who the authenticated user is.

Example:

```
SecurityContextHolder

↓

Current User

↓

Create Event
```

No organizer ID is required in the request.

---

# Transactions

Services that modify the database use:

```java
@Transactional
```

Example:

```
Create Event

↓

Find Hall

↓

Check Availability

↓

Save Event
```

If any step fails,

everything is rolled back automatically.

This ensures data consistency.

---

# Package Dependency Rules

A higher layer may use the layer below it.

```
Controller

↓

Service

↓

Repository
```

The opposite should never happen.

Examples:

✅ Controller → Service

✅ Service → Repository

❌ Repository → Service

❌ Entity → Repository

This keeps dependencies clean and avoids circular references.

---

# Current Module Architecture

```
Authentication

↓

Authorization

↓

Users

↓

Events

↓

Venues
```

Upcoming modules:

```
Hall

↓

Seat

↓

Booking

↓

Payment

↓

Notification
```

Each new module will follow the same layered architecture.

---

# Advantages of This Architecture

- Clear separation of responsibilities
- Easier debugging
- Better scalability
- Reusable services
- Cleaner controllers
- Better testing
- Easier onboarding for new developers
- Consistent project structure

---

# Summary

EventFlow follows a layered architecture where each layer has a single responsibility.

- **Controllers** handle HTTP communication.
- **Services** contain business logic.
- **Repositories** communicate with the database.
- **Entities** represent database tables.
- **DTOs** transfer data safely between the client and the server.

By keeping responsibilities separated, the project remains clean, maintainable, and scalable as new modules such as Hall, Seat, Booking, and Payment are added.