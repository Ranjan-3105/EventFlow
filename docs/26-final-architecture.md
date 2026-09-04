# 26. Final Architecture

## The Complete System

EventFlow is a modern, distributed web application built with a React frontend and a Spring Boot microservice-style backend (though currently deployed as a monolith). It leverages multiple infrastructure components to handle scale, reliability, and asynchronous processing.

### Architecture Diagram

```text
                    React 18 Frontend
                    (Vite, Tailwind, Shadcn)
                         |
                         | REST / JSON (over HTTP)
                         | JWT Authorization
                         ↓
                 Spring Boot 3 API
               (EventFlow Application)
                         |
       ┌─────────────────┼─────────────────┐
       ↓                 ↓                 ↓
 PostgreSQL 15        Redis 7           Kafka
 (Relational DB)    (In-Memory)     (Message Broker)
       |                 |                 |
       |                 |                 ↓
       |                 |        NotificationConsumer
       |                 |           (@KafkaListener)
       |                 |                 |
       |                 |                 ↓
       |                 |            EmailService
       |                 |                 |
       |                 |                 ↓
       |                 |             Brevo SMTP
       |                 |                 |
       |                 |                 ↓
       |                 |             User Inbox
       |
       └──── Source of Truth
```

*(Note: **Razorpay** also integrates via the Backend for Order creation and Signature Verification, and via the Frontend for the actual payment checkout flow).*

## Component Responsibilities

### 1. PostgreSQL (The Source of Truth)
Handles durable business state. If it's not in PostgreSQL, it didn't happen permanently.
- Users, Roles, and Authentication data.
- Venues, Halls, and physical Seat Layouts.
- Published Events.
- Bookings, booked seats, and Payments.

### 2. Redis (Fast Coordination & Caching)
Handles transient data that requires extreme speed or automatic expiration.
- **Coordination:** Seat locking during checkout (10-minute TTL). Prevents race conditions and double-booking.
- **Caching:** Caches the list of published events (5-minute TTL). Drastically reduces database load for read-heavy home page visits.

### 3. Kafka (Asynchronous Event Delivery)
Decouples core business transactions from secondary side-effects.
- **Topics:** `user.registered`, `booking.confirmed`
- When a user registers or pays, the core transaction commits to PostgreSQL instantly, and a message is fired to Kafka.
- The `NotificationConsumer` reads these messages in the background and handles the slow process of sending emails.

### 4. Razorpay (Payment Gateway)
Handles financial transactions.
- We never store credit card details.
- We rely on cryptographic signatures to verify payment success.

### 5. Brevo (SMTP Provider)
Delivers transactional emails (welcome emails, booking tickets).

### 6. Spring Boot (Business Layer)
The orchestrator. It exposes REST endpoints, secures them with stateless JWTs, validates business rules, manages JPA transactions, and glues all the infrastructure components together.

### 7. React (Presentation Layer)
The client-side application running in the user's browser. It manages UI state, navigation, form validation, and handles the visual Razorpay checkout process.
