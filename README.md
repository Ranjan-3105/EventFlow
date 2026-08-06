# 🎟️ EventFlow

> A production-inspired Event Ticket Booking System built with **Spring Boot**, documenting the journey from a monolithic backend to a scalable distributed system.

EventFlow is a hands-on backend engineering project focused on understanding how real-world event booking platforms are designed. Rather than building isolated CRUD APIs, the project models production workflows while documenting every major architectural decision, business rule, and Spring Boot concept along the way.

---

# ✨ Current Features

## 🔐 Authentication & Authorization

- JWT Authentication
- Stateless Security
- Spring Security Integration
- Role-Based Access Control (RBAC)
- BCrypt Password Encryption

---

## 👥 User Management

- User Registration
- Secure Login
- Custom UserDetailsService
- Role-based Authorization

---

## 🏢 Venue & Hall Management

- Admin-only Venue Creation
- Hall Creation
- Automatic Hall Capacity Calculation
- Venue Validation
- Duplicate Hall Detection

---

## 💺 Seat Management

- Automatic Seat Layout Generation
- Batch Seat Creation
- Configurable Rows & Seats per Row
- Automatic Hall Capacity Calculation
- Duplicate Layout Prevention
- Seat Layout Validation

---

## 🎭 Event Management

- Organizer-only Event Creation
- Hall Availability Validation
- Event Scheduling Conflict Detection
- Draft Event Support

---

## ⚙️ Backend Infrastructure

- Layered Architecture
- DTO Pattern
- Spring Data JPA
- Global Exception Handling
- Bean Validation
- Transaction Management
- PostgreSQL Integration

---

# 🛠️ Tech Stack

| Category | Technologies |
|----------|--------------|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security, JWT |
| ORM | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Build Tool | Maven |
| Containerization | Docker |

### Planned

- Redis
- Apache Kafka
- Elasticsearch
- Docker Compose
- API Gateway
- AWS
- Microservices

---

# 📂 Project Structure

```text
src/main/java
│
├── config/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── exception/
├── repository/
├── security/
├── service/
└── EventflowApplication.java
```

---

# 🏗️ Project Architecture

```text
                Client
                   │
                   ▼
            Spring Security
                   │
                   ▼
             JWT Filter Chain
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

---

# 📚 Documentation

One of the primary goals of EventFlow is to document every major backend concept while implementing it.

Current documentation includes:

- Spring Boot Fundamentals
- PostgreSQL & Docker Setup
- Project Architecture
- Spring Security & JWT
- Request Lifecycle
- Exception Handling
- Admin & Organizer Workflow
- Domain Model
- JPA Relationships
- Query Derivation vs JPQL
- Validation vs Business Rules
- Event Module
- Seat Module

Documentation grows alongside every completed module.

---

# 🚀 Current Progress

## Environment

- [x] Spring Boot
- [x] Java 21
- [x] Maven
- [x] Docker
- [x] PostgreSQL

---

## Security

- [x] User Registration
- [x] Login
- [x] JWT Authentication
- [x] Stateless Sessions
- [x] Role-Based Authorization
- [x] Authentication Entry Point
- [x] Access Denied Handler

---

## Core Backend

- [x] Layered Architecture
- [x] DTO Pattern
- [x] Repository Layer
- [x] Service Layer
- [x] Controller Layer
- [x] Bean Validation
- [x] Global Exception Handling
- [x] Transaction Management

---

## Modules

- [x] User
- [x] Venue
- [x] Hall
- [x] Seat
- [x] Event
- [ ] Booking
- [ ] Payment
- [ ] Ticket
- [ ] Notification

---

# 🗺️ Roadmap

## Booking System

- Booking Engine
- Redis Seat Locking
- Payment Integration
- Ticket Generation
- Booking History

## Scalability

- Redis Caching
- Kafka Event Streaming
- Elasticsearch
- API Gateway
- Docker Compose
- Microservices
- AWS Deployment

---

# 🎯 Learning Objectives

The purpose of EventFlow is not only to build an event booking system but also to understand how enterprise backend systems are designed.

Each module focuses on:

- Understanding the business problem
- Designing the domain model
- Implementing business rules
- Applying Spring Boot best practices
- Writing production-style documentation
- Thinking about scalability from the beginning

The repository serves as both a production-inspired backend project and a personal backend engineering handbook.

---

# 📈 Domain Model

```text
                   User
                     │
          ┌──────────┴──────────┐
          │                     │
       ADMIN               ORGANIZER
          │                     │
          ▼                     ▼
       Venue ───────────────► Event
          │                     ▲
          ▼                     │
        Hall ───────────────────┘
          │
          ▼
        Seats
```

---

# 🚀 Upcoming Architecture

```text
Venue
  │
  ▼
Hall
  │
  ▼
Seat
  │
  ▼
Booking
  │
  ▼
Payment
  │
  ▼
Ticket
```

Eventually:

```text
Client
   │
API Gateway
   │
┌──────┬────────┬────────┬────────┐
│      │        │        │
User  Event  Booking  Payment
Service Service Service Service
               │
            Redis
               │
            Kafka
```

---

# ⭐ Project Status

🚧 **Actively under development**

Completed modules are fully tested with validation, exception handling, and business rules before moving to the next feature.

The next major milestone is building the **Booking Engine**, followed by **Redis-based seat locking**, **Payment Integration**, and **Kafka-driven asynchronous workflows**.