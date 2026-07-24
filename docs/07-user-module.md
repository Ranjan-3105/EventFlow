# 07. User Module

## Overview

The User Module is responsible for registering new users into the EventFlow system.

The implementation follows a layered architecture:

```
Client
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

Each layer has a single responsibility, making the application easier to maintain, test, and extend.

---

# Project Structure

```
user
├── controller
│   └── UserController
│
├── service
│   └── UserService
│
├── repository
│   └── UserRepository
│
├── dto
│   ├── request
│   │   └── CreateUserRequest
│   │
│   └── response
│       └── UserResponse
│
└── entity
    └── User
```

---

# Components

## 1. Request DTO

### CreateUserRequest

Represents the data sent by the client during registration.

### Purpose

- Accept client input
- Validate incoming data
- Prevent invalid requests from reaching the service layer

### Fields

- firstName
- lastName
- email
- password
- phoneNumber
- dateOfBirth

### Validation

- `@NotBlank`
- `@Email`
- `@Size`
- `@Pattern`
- `@NotNull`

---

## 2. Entity

### User

Represents the `users` table in PostgreSQL.

### Client Controlled Fields

- firstName
- lastName
- email
- password
- phoneNumber
- dateOfBirth

### System Controlled Fields

- id
- role
- enabled
- createdAt
- updatedAt

These values are managed by the backend and are never accepted from the client.

---

## 3. Response DTO

### UserResponse

Represents the data returned after successful registration.

### Fields

- id
- firstName
- lastName
- email
- phoneNumber
- role

### Why not return the password?

Passwords are confidential and should never leave the backend after being stored.

---

## 4. Repository

### UserRepository

```java
public interface UserRepository
        extends JpaRepository<User, UUID> {
}
```

### Responsibilities

- Save users
- Fetch users
- Delete users
- Update users

Spring Data JPA automatically generates CRUD operations.

### Custom Queries

```java
findByEmail(String email)
```

Spring derives the SQL automatically from the method name.

---

## 5. Service

### UserService

Contains the application's business logic.

### Responsibilities

- Validate business rules
- Check if email already exists
- Convert DTO → Entity
- Populate internal fields
- Save entity
- Convert Entity → DTO

Business logic should never reside inside the Controller.

---

## 6. Controller

### UserController

Responsible for handling HTTP requests.

### Endpoint

```
POST /api/users/register
```

### Responsibilities

- Accept HTTP request
- Validate request body
- Delegate processing to UserService
- Return HTTP response

The Controller should remain thin and contain no business logic.

---

# Request Flow

```
Client

    │

POST /api/users/register

    │

    ▼

UserController

    │

    ▼

CreateUserRequest

    │

    ▼

UserService

    │

Check Email

    │

Map DTO → Entity

    │

Populate Internal Fields

    │

    ▼

UserRepository

    │

    ▼

PostgreSQL

    │

    ▼

Saved User

    │

Map Entity → DTO

    │

    ▼

UserResponse
```

---

# Security

Registration is intentionally exposed as a public endpoint.

```
POST /api/users/register
```

is accessible without authentication.

All other endpoints currently require authentication through Spring Security.

---

# Current Validation

Implemented:

- Email uniqueness (service layer)

Database Constraints:

- Unique email
- Unique phone number

> **Note:** Phone number uniqueness is currently enforced by the database. A service-layer validation will be added later for better error messages.

---

# Design Decisions

- UUID used as the primary key.
- DTOs isolate the API contract from the database model.
- Password is never returned in API responses.
- Role is assigned internally (`USER`).
- `enabled` defaults to `true`.
- Repository handles persistence only.
- Service contains business logic.
- Controller handles HTTP communication only.

---

# Future Improvements

- BCrypt password hashing
- Global exception handling (`@ControllerAdvice`)
- Custom exceptions
- JWT authentication
- Login endpoint
- Email verification
- Refresh tokens
- Role-based authorization