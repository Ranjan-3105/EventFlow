# 🎟️ EventFlow

> A production-inspired Event Ticket Booking System built with **Spring Boot**, focusing on learning backend engineering through real-world implementation.

EventFlow is more than a CRUD application. The project is designed to simulate the architecture of large-scale event booking platforms while documenting every major concept learned along the way.

---

# ✨ Features Implemented

## 🔐 Authentication & Authorization

- JWT Authentication
- Stateless Security
- Spring Security Integration
- Role-Based Authorization
- BCrypt Password Encryption

## 👤 User Management

- User Registration
- User Login
- Custom UserDetailsService

## 🎭 Event Management

- Event Creation
- Hall Availability Validation
- Scheduling Conflict Detection
- Organizer Authentication

## 🏢 Venue Management

- Admin-Only Venue Creation
- Geographic Coordinates Support
- Validation & Exception Handling

## ⚙️ Backend Infrastructure

- Layered Architecture
- DTO Pattern
- Global Exception Handling
- Request Validation
- Transaction Management
- Spring Data JPA
- PostgreSQL Integration

---

# 🛠️ Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)
- JWT

### Database

- PostgreSQL

### DevOps

- Docker
- Maven

### Planned

- Redis
- Apache Kafka
- Elasticsearch
- Docker Compose
- AWS
- API Gateway
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

The project follows a layered architecture:

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

Every major concept is documented inside the **docs/** directory.

Current documentation includes:

- Project Setup
- Docker Setup
- Security Architecture
- Project Architecture
- Exception Handling
- Spring Boot Core Concepts

More documentation is added alongside every completed module.

---

# 🚧 Current Progress

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
- [x] Role-Based Authorization
- [x] Stateless Sessions
- [x] Authentication Entry Point
- [x] Access Denied Handler

---

## Backend

- [x] DTO Layer
- [x] Repository Layer
- [x] Service Layer
- [x] Controller Layer
- [x] Validation
- [x] Global Exception Handling
- [x] Transaction Management

---

## Modules

- [x] User
- [x] Event
- [x] Venue
- [ ] Hall
- [ ] Seat
- [ ] Booking
- [ ] Payment
- [ ] Ticket
- [ ] Notification

---

## Upcoming Features

- Seat Locking
- Redis Integration
- Kafka Integration
- Payment Gateway
- Booking Engine
- Event Search
- Email Notifications
- Microservices Migration

---

# 🎯 Learning Goal

The purpose of this project is not only to build an event booking system but also to understand how enterprise backend applications are designed.

Every feature is implemented from scratch while documenting:

- Why it exists
- How it works internally
- Where it fits in a production system
- Best practices followed during implementation

The repository serves as both a project and a personal backend engineering handbook.

---

## ⭐ Project Status

**Actively under development.** New modules and documentation are added incrementally as the project evolves.