# 🎟️ EventFlow

> A production-inspired Event Ticket Booking System built with **Spring Boot**, documenting the journey from a monolithic backend to a scalable distributed system.

EventFlow is a hands-on learning project focused on backend engineering. Instead of building isolated CRUD APIs, the project models real-world event management workflows while documenting every architectural decision, concept, and implementation.

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

## 🏢 Venue Management

- Admin-only Venue Creation
- Hall Management
- Venue Validation
- Duplicate Hall Detection

---

## 🎭 Event Management

- Organizer-only Event Creation
- Hall Availability Validation
- Event Scheduling Conflict Detection
- Event Status Management (Draft)

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

```
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

Project Architecture

```
Client

↓

Controller

↓

Service

↓

Repository

↓

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
- Event Module Deep Dive

Documentation is expanded alongside every completed module.

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
- [x] Validation
- [x] Global Exception Handling
- [x] Transaction Management

---

## Modules

- [x] User
- [x] Venue
- [x] Hall
- [x] Event
- [ ] Seat
- [ ] Booking
- [ ] Payment
- [ ] Ticket
- [ ] Notification

---

# 🗺️ Roadmap

## Booking System

- Seat Management
- Seat Locking
- Booking Engine
- Payment Integration
- Ticket Generation

## Scalability

- Redis Caching
- Kafka Event Streaming
- Elasticsearch
- API Gateway
- Microservices
- AWS Deployment

---

# 🎯 Learning Objectives

This project is built to understand how production backend systems are designed rather than simply creating CRUD APIs.

Every module focuses on:

- Understanding the problem
- Designing the domain model
- Implementing business rules
- Applying Spring Boot best practices
- Documenting architectural decisions

The repository serves as both a production-inspired backend project and a personal backend engineering handbook.

---

# 📈 Current Architecture

```
                User
                  │
        ┌─────────┴─────────┐
        │                   │
     ADMIN             ORGANIZER
        │                   │
        ▼                   ▼
      Venue ───────────► Event
        │                  ▲
        ▼                  │
       Hall ───────────────┘
```

Upcoming

```
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

---

# ⭐ Project Status

🚧 **Actively under development**

The project is being developed module by module while documenting each implementation. Future milestones include the booking engine, Redis-based seat locking, Kafka-driven event processing, and a transition towards a microservices architecture.