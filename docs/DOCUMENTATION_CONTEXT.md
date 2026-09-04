# Documentation Context

## 1. Documentation Philosophy

**Why the documentation exists:**
The documentation serves as a chronological, detailed learning and development record. It is designed to capture the journey of building a production-grade Event Ticket Booking System using Spring Boot, explaining not just *what* was built, but *why* specific architectural decisions were made.

**How detailed it is:**
The documentation is highly detailed, combining both tutorial-like explanations of core concepts with implementation-like breakdowns of the actual code. It acts as both a learning resource and a project portfolio.

**How concepts are introduced:**
Concepts are introduced incrementally on a "need-to-know" basis. The philosophy is "Learn by Implementing." A requirement is identified, the relevant concept is explained, and then it is implemented. 

**How code decisions are explained:**
Code decisions are justified with practical examples of real-world scenarios. For example, the choice of using UUIDs is explained through the lens of distributed systems. The difference between Authentication and Authorization is clarified before showing the security filter chain.

**Emphasis:**
The docs strongly emphasize "why" over "what" and "how". They focus heavily on the reasoning behind architectural decisions (e.g., why to use a separate Booking entity from a Ticket, why to use `@RestControllerAdvice` for exceptions, why Redis locking precedes PostgreSQL checks).

## 2. Documentation Style

- **Heading hierarchy:** Uses Markdown `#` for main topics/modules, `##` for subtopics/components, and `---` horizontal rules to separate major sections.
- **Explanation depth:** Explanations begin with high-level conceptual analogies (e.g., comparing Docker images to Java classes) and drill down into specific Spring Boot annotations and request lifecycles.
- **Terminology:** Consistent use of enterprise Java and Spring terminology (e.g., IoC, Dependency Injection, Entities, DTOs, Controllers, Services, Repositories).
- **Code-block usage:** Heavy use of short, focused code blocks. Avoids pasting massive files. Instead, it highlights specific annotations, DTO structures, or JPQL queries.
- **Diagrams:** Uses simple text-based ASCII/Markdown flowcharts (e.g., `A ↓ B ↓ C`) to illustrate request lifecycles, database relationships, and component interactions.
- **Tables:** Used occasionally for summarizing HTTP status codes or listing roles and permissions.
- **Examples:** Real-world examples are frequently used to explain business rules (e.g., "Hall 1, Hall 2", "10:00 to 13:00 vs 11:00 to 14:00").
- **Implementation walkthroughs:** Walkthroughs are conceptual. They trace a request from the Client → Controller → Service → Repository → Database, showing how data flows and where validation happens, rather than line-by-line code tutorials.
- **Common patterns:** 
  - Using "Purpose", "Example", "Business Rules", and "Relationships" sections to describe entities.
  - "Traditional Approach" vs "Spring Approach" to justify framework features.
  - Summarizing the "Request Flow" with vertical text diagrams.

*Representative Examples:*
1. **Flow Diagrams:** Vertical text flows are used extensively to show lifecycles. (e.g., `Client \n ↓ \n Controller \n ↓ \n Service...`)
2. **"Why not X?" sections:** The docs frequently ask "Why not return entities?" or "Why not use Query Derivation here?" to contrast bad practices with the implemented best practices.
3. **Business Rule breakdowns:** Instead of just listing code, rules are written in plain English (e.g., "Start time must be before end time", "Hall cannot be booked twice for overlapping time intervals").

## 3. Chronological Learning Path

| # | Document | Main Concept | What Was Learned | What Was Implemented |
|---|---|---|---|---|
| 01 | 01-ProjectSetup.md | Project Vision | The "Learn by Implementing" philosophy, architectural phases (Monolith to Microservices). | Initial repository structure and roadmap. |
| 02 | 02-docker.md | Containerization | Docker basics: Images, Containers, Port Mapping, Volumes. | Running PostgreSQL in a Docker container. |
| 03 | 03-postgresql.md | Database Basics | Default PostgreSQL databases (`postgres`, `eventdb`), connecting via `psql`. | Created `eventdb` for the application. |
| 04 | 04-spring-boot.md | Application Lifecycle | Spring Boot startup sequence (JVM → ApplicationContext → Tomcat). | Basic Spring Boot application initialization. |
| 05 | 05-domain-modelling.md | Domain-Driven Design | Single responsibility principle for entities, derived attributes, UUIDs. | Conceptual models for User, Venue, Screen, Seat, Event, Booking, Ticket. |
| 06 | 06-spring-core.md | IoC & Dependency Injection | Beans, Spring Container, layered architecture responsibilities. | Abstracted Controller/Service/Repository layers. |
| 07 | 07-user-module.md | API Layering & DTOs | Request/Response DTO pattern, keeping controllers thin, hiding internal fields. | User Registration API (`CreateUserRequest`, `UserResponse`). |
| 08 | 08-docker-setup.md | Infrastructure Ops | Managing Docker Compose, reading logs, connecting with external clients (DBeaver). | Operational control of the local PostgreSQL container. |
| 09 | 09-security-architecture.md | Stateless Authentication | JWT concepts, Authentication vs Authorization, Spring Security filter chain. | JWT Generation/Validation, BCrypt hashing, Role-based access control. |
| 10 | 10-project-architecture.md | Layered Architecture | Separation of concerns, dependency injection rules (Controller → Service → Repository). | Structured packages (`controller`, `service`, `repository`, `dto`, `entity`). |
| 11 | 11-exception-handling.md | Global Error Handling | `@RestControllerAdvice`, consistent JSON error responses, validation errors. | Custom exceptions (e.g., `UserAlreadyExistsException`), unified error mapping. |
| 12 | 12-spring-boot-core-concepts.md | Spring Ecosystem | `@Service`, `@Repository`, Bean validation, JPA basics, `@Transactional`. | Applied annotations across existing modules. |
| 13 | 13-admin-organizer-workflow.md | RBAC & Workflows | Role separation (ADMIN vs ORGANIZER), business rule enforcement at the service layer. | Admin APIs for Venue/Hall creation; Organizer workflow context. |
| 14 | 14-domain-model.md | Entity Definitions | Mapping Java classes to database tables via JPA, defining fields and constraints. | JPA entities for User, Venue, Hall, Event. |
| 15 | 15-jpa-relationships.md | Database Relationships | `@OneToMany`, `@ManyToOne`, `mappedBy`, `FetchType.LAZY`, Foreign Keys. | Linked Venue → Hall and Organizer/Hall → Event. |
| 16 | 16-query-derivation-vs-jpql.md | Data Access Patterns | Spring Data query derivation (`existsBy...`) vs custom JPQL queries. | Hall uniqueness validation (Derivation) and Event conflict detection (JPQL). |
| 17 | 17-validation-vs-business-rules.md | Validation Strategy | Jakarta Bean Validation (Input) vs Service-layer database checks (Business). | `@Valid` on DTOs; `hallRepository.exists...` in Services. |
| 18 | 18-event-module.md | Complex Business Logic | Cross-entity validation, time interval overlap detection, deriving user from context. | Event creation API with hall availability validation. |
| 19 | 19-seat-module.md | Automated Generation | Batch persistence, dynamic layout calculation, unique constraints on combinations. | Automatic generation of Hall seat layouts based on rows/columns. |
| 20 | 20-booking-module.md | Concurrency & Locking | Redis distributed locking, TTL, atomic operations, pessimistic concurrency control. | Multi-seat booking reservation with Redis locks + PostgreSQL state. |

## 4. Concepts Already Covered

- **Spring Boot:** Application lifecycle, auto-configuration, dependency injection, layered architecture. EventFlow uses it as the foundational backend framework.
- **Spring Core:** Inversion of Control, Beans, Stereotype annotations (`@Service`, `@RestController`). Used to manage object lifecycles and dependencies.
- **JPA / Hibernate:** Entities, table mapping, `@OneToMany`/`@ManyToOne` relationships, Lazy loading, Query derivation, JPQL. Used to map domain objects to PostgreSQL.
- **PostgreSQL:** Relational data storage, foreign keys, unique constraints. Used as the persistent source of truth.
- **Security:** Stateless JWT authentication, Spring Security filter chain, `SecurityContextHolder`, Role-Based Access Control (RBAC). Used to protect APIs and derive user identity from tokens.
- **Redis:** Distributed caching, Atomic operations (`SET NX`), Time-To-Live (TTL). Used for distributed lock management during seat booking to prevent race conditions.
- **Validation / Exception Handling:** Jakarta Bean Validation (`@Valid`, `@NotBlank`), `@RestControllerAdvice`, custom exceptions. Used to reject bad requests early and return consistent JSON errors.
- **Architecture:** Controller-Service-Repository pattern, DTO pattern (Request/Response), Separation of input validation from business validation.
- **Docker:** Images, containers, volumes, port mapping, `docker-compose`. Used to run infrastructure dependencies locally.

## 5. Important Architectural Decisions

- **Decision:** Use UUIDs instead of Long for primary keys.
  - **Reason:** Globally unique, easier for distributed systems, prevents ID guessing.
  - **Implementation:** Entity IDs are defined as `UUID` and generated automatically.
  - **Relevant documentation:** `05-domain-modelling.md`, `14-domain-model.md`
- **Decision:** Stateless JWT authentication instead of sessions.
  - **Reason:** Highly scalable, RESTful, no server-side session state required.
  - **Implementation:** `SessionCreationPolicy.STATELESS`, custom `JwtAuthenticationFilter`.
  - **Relevant documentation:** `09-security-architecture.md`
- **Decision:** Separate DTOs for Requests and Responses.
  - **Reason:** Prevents exposing internal entity states (like passwords), prevents mass-assignment vulnerabilities, decouples API contracts from database schemas.
  - **Implementation:** `dto/request` and `dto/response` packages.
  - **Relevant documentation:** `07-user-module.md`, `10-project-architecture.md`
- **Decision:** Controller-Service-Repository layered architecture.
  - **Reason:** Separation of concerns. Controllers handle HTTP, Services handle business logic, Repositories handle data access.
  - **Implementation:** Strict package dependencies; controllers only call services, never repositories.
  - **Relevant documentation:** `10-project-architecture.md`
- **Decision:** Global Exception Handling via `@RestControllerAdvice`.
  - **Reason:** Keeps controllers clean, provides consistent JSON error responses, centralized error management.
  - **Implementation:** `GlobalExceptionHandler` intercepting custom exceptions.
  - **Relevant documentation:** `11-exception-handling.md`
- **Decision:** Use `FetchType.LAZY` for JPA relationships.
  - **Reason:** Prevents N+1 query problems and unnecessary data loading.
  - **Implementation:** `@ManyToOne(fetch = FetchType.LAZY)`.
  - **Relevant documentation:** `15-jpa-relationships.md`
- **Decision:** Redis locks for seat booking concurrency, PostgreSQL for persistence.
  - **Reason:** Redis acts as a fast concurrency gate to prevent race conditions without heavily locking database rows.
  - **Implementation:** `SET NX EX` in Redis followed by PostgreSQL availability checks.
  - **Relevant documentation:** `20-booking-module.md`
- **Decision:** Separate Booking from Ticket generation.
  - **Reason:** Allows seat reservation before payment processing.
  - **Implementation:** Booking starts in `PENDING` state with Redis TTL locks.
  - **Relevant documentation:** `05-domain-modelling.md`

## 6. Terminology / Naming Conventions

- **Entity names:** `User`, `Venue`, `Hall`, `Event`, `Seat`, `Booking`, `BookingSeat`. (Singular nouns)
- **DTO names:** Suffixed with `Request` or `Response` (e.g., `CreateUserRequest`, `UserResponse`).
- **Service names:** Suffixed with `Service` (e.g., `EventService`, `SeatLockService`).
- **Repository names:** Suffixed with `Repository` (e.g., `VenueRepository`).
- **Exception names:** Descriptive, business-focused, suffixed with `Exception` (e.g., `HallAlreadyBookedException`, `InvalidEventTimeException`).
- **Architectural terminology:** Input Validation (format checks) vs Business Validation (database checks).
- **Domain terminology:** 
  - `Admin`: Manages venues and halls.
  - `Organizer`: Creates and manages events.
  - `User`: Books tickets.
  - `Hall`: The physical auditorium.
  - `Event`: The logical show scheduled in a Hall.

## 7. Current Project State

Based on the source code, EventFlow currently implements:

- **Authentication:** Implemented (JWT, BCrypt, AuthController)
- **Users:** Implemented (Registration, Role assignment)
- **Venues/halls:** Implemented (Admin APIs)
- **Seats:** Implemented (Automatic generation via SeatController)
- **Seat locking:** Implemented (Redis `SeatLockService`)
- **Bookings:** Implemented (PENDING and CONFIRMED states, `BookingExpirationService`)
- **Payments:** Implemented (Razorpay integration via `PaymentController` / `PaymentService`)
- **Razorpay:** Implemented (Order creation, Signature verification)
- **Redis:** Implemented (Seat locks, Event caching via `EventCacheService`)
- **Kafka:** Implemented (`KafkaEventProducer`, `NotificationConsumer`)
- **Email notifications:** Implemented (`EmailService` consuming Kafka events)
- **Frontend:** Implemented (React, Vite, TypeScript, Tailwind, Shadcn UI)
- **Security:** Implemented (Stateless JWT)

**Implementation Status:**
- Backend API: Fully Implemented
- Frontend Event Browsing: Implemented (`Events.tsx`)
- Frontend Event Details: Implemented (`EventDetails.tsx`)
- Frontend Seat Selection: Implemented (`SeatSelection.tsx`)
- Frontend Booking/Payment UI: Implemented (`BookingPayment.tsx`, `BookingSuccess.tsx`)
- Frontend My Bookings: Implemented (`MyBookings.tsx`)
- Auth Context: Implemented (`context/`)
- Dark/Light mode: Implemented (Frontend)

## 8. What Happened After Document 20

Following the initial booking module (Doc 20), several major systems were integrated to complete the platform:

1. **Payment Module (Razorpay):** A `PaymentService` was added to integrate with Razorpay. It creates Razorpay Orders for `PENDING` bookings and verifies cryptographic signatures from the client upon payment completion, transitioning bookings to `CONFIRMED`.
2. **Booking Expiration:** A scheduled task (`BookingExpirationService`) was added to automatically cancel `PENDING` bookings if payment is not completed within the allowed window.
3. **Kafka & Email Notifications:** Asynchronous event streaming was implemented. Upon successful payment verification, a `BookingConfirmedEvent` is published to Kafka via `KafkaEventProducer`. A `NotificationConsumer` listens to this topic and delegates to an `EmailService` to send confirmation emails via Brevo SMTP.
4. **Caching:** An `EventCacheService` was introduced using Redis to cache event details, improving read performance for the frontend.
5. **React Frontend:** A complete, modern Single Page Application was built in `frontend/` using React 19, TypeScript, Vite, Tailwind CSS, and Shadcn UI components.
6. **Frontend Features:** The frontend fully consumes the backend APIs, providing:
   - User authentication flows (Login/Register)
   - Event browsing and detailed views
   - Interactive seat selection UI
   - Integration with the Razorpay checkout modal
   - Booking success confirmation and user booking history ("My Bookings")

## 9. Existing Documentation Gaps

| Gap | Priority | Reason |
|---|---|---|
| Payment Integration (Razorpay) | HIGH | Complex flow involving 3rd party APIs, order creation, and cryptographic signature verification. Critical for understanding the transition from PENDING to CONFIRMED. |
| Kafka and Asynchronous Processing | HIGH | Introduces event-driven architecture, producer/consumer patterns, and decoupling of core business logic from notification logic. |
| React Frontend Architecture | MEDIUM | The project transitioned from purely backend to full-stack. The frontend architecture, Vite proxying, state management, and Shadcn usage need explanation. |
| Redis Caching | MEDIUM | Redis is used for more than just locks now (Event caching). The caching strategy and invalidation rules should be documented. |
| Scheduled Tasks (Booking Expiration) | LOW | Explaining `@Scheduled` and how stale PENDING bookings are cleaned up complements the booking lifecycle. |

## 10. Recommended Next Documentation Sequence

The natural next sequence should bridge the gap between reserving seats and confirming the reservation.

**Next Document:** `21-payment-module.md`

**Why it belongs next:**
Document 20 ended with a booking in the `PENDING` state. The logical next step in the user journey and system architecture is paying for that booking to confirm it.

**Concepts it should teach:**
- Integrating 3rd party payment gateways (Razorpay).
- The difference between Payment Orders and Payment Verification.
- Webhook/Signature verification to prevent client-side payment spoofing.
- State transitions (PENDING → CONFIRMED).

**What it should reference:**
- The existing `BookingService` and `PENDING` state from Doc 20.
- The `PaymentController` and `PaymentService` implementations.

**What it should NOT repeat:**
- It should not repeat how a Booking is created or how Redis locks work, as Doc 20 covers this.

*Subsequent natural documents:*
- `22-kafka-async-notifications.md` (What happens *after* a booking is confirmed).
- `23-caching-with-redis.md` (Optimizing the read-heavy event browsing APIs).
- `24-frontend-architecture.md` (How the React app consumes these APIs).

## 11. Important Continuity Rules

For the next AI/documentation writer:

1. **Preserve the Philosophy:** Maintain the "Learn by Implementing" approach. Explain the *concept* and the *why* before showing the code.
2. **Match the Tone:** Keep explanations concise, use vertical flow diagrams, and break down complex concepts into simple analogies.
3. **No Rewriting History:** Do not silently change decisions made in Docs 01-20. Build upon them.
4. **Avoid Code Dumps:** Do not paste entire classes. Show only the relevant snippets (e.g., a specific method or annotation).
5. **Distinguish Code vs Concept:** Clearly separate "what Spring Boot does natively" from "what EventFlow implements specifically".
6. **Chronological Progression:** Assume the reader has read the previous documents. Do not re-explain JWT, layered architecture, or basic JPA if they were covered earlier.
7. **Use Absolute File Paths:** If referencing specific files in the repository, ensure accuracy based on the current `Backend/` and `frontend/` directory structure.
8. **Document the Reality:** Base documentation on the *actual* code in the repository, not on theoretical best practices that haven't been implemented yet.
