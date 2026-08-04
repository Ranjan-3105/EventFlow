# 🌱 Spring Boot Core Concepts

> **Framework:** Spring Boot  
> **Core Topics:** IoC, Dependency Injection, Beans, Transactions, JPA

---

# 📖 Overview

Spring Boot is more than just a web framework.

It manages the lifecycle of objects, automatically injects dependencies, handles transactions, validates requests, maps HTTP endpoints, communicates with databases, and provides many enterprise-level features with minimal configuration.

Throughout EventFlow, we rely heavily on these core Spring concepts.

Understanding them is essential to understanding how the project works.

---

# What is Spring?

Spring is a Java framework that helps developers build enterprise applications.

Instead of manually creating and managing objects, Spring creates them for us.

Without Spring:

```java
UserRepository repository = new UserRepository();

UserService service = new UserService(repository);

UserController controller = new UserController(service);
```

With Spring:

```java
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

Spring automatically creates everything.

---

# Inversion of Control (IoC)

One of Spring's biggest ideas is **Inversion of Control**.

Normally, your application creates objects.

```
Application

↓

new UserService()

↓

new UserRepository()
```

With Spring,

the framework creates and manages the objects.

```
Spring Container

↓

Creates Objects

↓

Application Uses Them
```

The control of object creation is "inverted" from the application to Spring.

---

# Spring Container

The Spring Container is responsible for:

- Creating objects
- Managing objects
- Injecting dependencies
- Destroying objects when needed

Every object managed by Spring is called a **Bean**.

---

# What is a Bean?

A Bean is simply an object managed by Spring.

Example:

```java
@Service
public class EventService {
}
```

When the application starts,

Spring creates one instance of `EventService`.

Whenever another class needs it,

Spring provides the same instance.

---

# Common Spring Stereotype Annotations

Spring uses annotations to identify beans.

## @RestController

Marks a class as a REST Controller.

Responsibilities:

- Handle HTTP requests
- Return JSON responses

Example:

```java
@RestController
public class EventController {
}
```

---

## @Service

Marks a class as the business layer.

Responsibilities:

- Business logic
- Coordination
- Validation
- Transactions

Example:

```java
@Service
public class EventService {
}
```

---

## @Repository

Marks a class or interface responsible for database operations.

Example:

```java
@Repository
public interface UserRepository
        extends JpaRepository<User, UUID> {
}
```

Spring automatically creates the implementation.

---

## @Configuration

Used for configuration classes.

Example:

```java
@Configuration
public class SecurityConfig {
}
```

---

# Dependency Injection (DI)

One bean often depends on another.

Example:

```
Controller

↓

Service

↓

Repository
```

Instead of creating these objects manually,

Spring injects them.

Example:

```java
private final UserService userService;

public UserController(UserService userService) {
    this.userService = userService;
}
```

Spring sees:

```
UserController

↓

Needs UserService

↓

Inject Existing Bean
```

This is called **Constructor Injection**.

---

# Why Constructor Injection?

Advantages:

- Immutable dependencies
- Easier testing
- Better readability
- Dependencies are explicit
- Prevents NullPointerException

This is the recommended approach in modern Spring applications.

---

# Request Lifecycle

A request follows this path.

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

↓

Repository

↓

Service

↓

Controller

↓

Client
```

Every layer has a specific responsibility.

---

# Spring Data JPA

Spring Data JPA simplifies database operations.

Instead of writing SQL manually,

we extend:

```java
JpaRepository<Entity, ID>
```

Example:

```java
public interface UserRepository
        extends JpaRepository<User, UUID> {
}
```

Spring automatically provides methods such as:

- save()
- findById()
- findAll()
- delete()
- existsById()

without writing any implementation.

---

# Hibernate

Hibernate is the ORM (Object Relational Mapping) framework used by Spring Data JPA.

Instead of writing SQL,

we work with Java objects.

Example:

```java
User user = new User();

userRepository.save(user);
```

Hibernate automatically generates the SQL.

---

# Entity

An Entity represents a database table.

Example:

```java
@Entity
@Table(name = "users")
public class User {
}
```

Each object becomes one row in the database.

---

# Repository

Repositories communicate with the database.

Example:

```java
userRepository.findByEmail(email);
```

The repository performs the query.

The Service decides what to do with the result.

---

# Transactions

Some operations involve multiple database actions.

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

If saving the event fails,

the previous operations should not leave the database in an inconsistent state.

Spring solves this using:

```java
@Transactional
```

If any exception occurs,

the transaction is rolled back automatically.

---

# Validation

Incoming requests are validated before reaching the Service.

Example:

```java
@NotBlank

@NotNull

@Email
```

Controller:

```java
public ResponseEntity<?> create(
        @Valid @RequestBody Request request
)
```

If validation fails,

Spring throws:

```
MethodArgumentNotValidException
```

which is handled by the Global Exception Handler.

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

Controllers and Services can access the authenticated user without asking the client again.

---

# Why Layered Architecture Works Well with Spring

Spring's dependency injection naturally fits layered architecture.

```
Controller

↓

Service

↓

Repository
```

Each layer depends only on the layer below it.

This keeps the application:

- Modular
- Testable
- Maintainable
- Easy to extend

---

# Current Spring Components Used in EventFlow

| Annotation | Purpose |
|------------|---------|
| @RestController | Handle HTTP requests |
| @Service | Business logic |
| @Repository | Database access |
| @Configuration | Application configuration |
| @Bean | Create custom Spring beans |
| @Entity | Database table |
| @Table | Table mapping |
| @Id | Primary key |
| @GeneratedValue | Generate IDs |
| @ManyToOne | Relationship mapping |
| @OneToMany | Relationship mapping |
| @Transactional | Database transaction |
| @Valid | Request validation |

---

# Best Practices Followed

- Constructor Injection
- Layered Architecture
- DTO Pattern
- Global Exception Handling
- Stateless Authentication
- Separation of Concerns
- Transactional Services
- Repository Pattern

---

# Summary

Spring Boot handles much more than HTTP requests.

It creates and manages objects, injects dependencies, validates requests, manages database transactions, communicates with the database through JPA, and integrates security seamlessly.

By leveraging these features, EventFlow remains clean, modular, and scalable while reducing boilerplate code and following enterprise development practices.