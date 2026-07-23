# 06. Spring Core

## Objective

Understand how Spring manages object creation and dependency management.

---

## Key Concepts

### Bean

A Bean is an object whose lifecycle is managed by the Spring IoC Container.

Examples:

- UserService
- UserRepository
- UserController

Non-beans:

- User Entity
- Venue Entity
- Booking Entity

These are created by the application itself.

---

### IoC (Inversion of Control)

Instead of classes creating their own dependencies using `new`, Spring creates and manages all required objects.

Traditional Java

BookingService

↓

new UserRepository()

↓

new Database()

Spring

Spring Container

↓

UserRepository Bean

↓

BookingService Bean

---

### Dependency Injection

Dependencies are provided through constructors.

Example

```java
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

The service does not create its repository.

Spring injects it.

---

### Repository

Repositories abstract database access.

Responsibilities:

- Save entities
- Retrieve entities
- Delete entities

Repositories should not contain business logic.

Business rules belong inside Services.

---

### Service

The Service layer contains business logic.

Responsibilities:

- Validation
- Business rules
- Coordinating multiple repositories
- Mapping DTOs ↔ Entities

---

## Architectural Flow

HTTP Request

↓

Controller

↓

Service

↓

Repository

↓

Hibernate

↓

PostgreSQL