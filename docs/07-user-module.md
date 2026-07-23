# 07. User Module

## Goal

Implement user registration following a production-style layered architecture.

---

## Layers

### Request DTO

CreateUserRequest

Purpose:

- Accept client input
- Perform request validation

Contains:

- firstName
- lastName
- email
- password
- phoneNumber
- dateOfBirth

---

### Entity

User

Represents the database model.

Contains internal fields:

- id
- enabled
- role
- createdAt
- updatedAt

These fields are never supplied by the client.

---

### Response DTO

UserResponse

Returns only information the client needs.

Contains:

- id
- firstName
- lastName
- email
- phoneNumber
- role

Password is intentionally excluded.

---

### Repository

UserRepository

Extends JpaRepository<User, UUID>.

Provides CRUD operations without writing SQL.

---

### Service

UserService

Responsibilities:

- Receive CreateUserRequest
- Map Request → Entity
- Populate internal fields
- Persist User
- Map Entity → UserResponse

---

## Request Flow

POST /api/users

↓

CreateUserRequest

↓

UserService

↓

UserRepository

↓

Database

↓

UserResponse

---

## Design Decisions

- UUID used as primary key.
- Request and Response DTOs separate API contract from database model.
- Password never returned.
- Role assigned internally as USER.
- Enabled defaults to true.
- Repository contains persistence logic only.
- Business logic resides inside UserService.