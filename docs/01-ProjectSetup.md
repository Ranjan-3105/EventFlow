# Project Setup

# EventFlow

A production-grade Event Ticket Booking System built to learn backend development through practical implementation using Spring Boot.

Unlike tutorial-based projects, EventFlow is developed incrementally while understanding the concepts behind every architectural decision.

The objective is not only to build a working application but also to understand how production systems are designed.

---

# Objectives

This project aims to learn:

- Spring Boot fundamentals
- REST API development
- Database design
- JPA & Hibernate
- Spring Security & JWT
- Docker
- Redis
- Kafka
- Microservices
- API Gateway
- System Design
- Deployment

---

# Learning Approach

This project follows the principle:

> Learn by Implementing

Instead of learning individual Spring Boot concepts in isolation, every concept is introduced only when it becomes necessary.

Example:

```
Need user authentication
        ↓
Learn Spring Security
        ↓
Implement Authentication
        ↓
Understand how it works internally
```

This approach makes every concept easier to retain because it is immediately applied.

---

# Development Roadmap

The project will be developed in multiple phases.

## Phase 1 — Modular Monolith

Focus Areas:

- Spring Boot
- PostgreSQL
- JPA
- Hibernate
- Authentication
- Authorization
- Booking Logic
- Payment Simulation
- Docker

Objective:

Build a production-quality backend while understanding the core Spring ecosystem.

---

## Phase 2 — Distributed Systems

After the monolith is stable, the application will gradually be decomposed into independent services.

Services planned:

- User Service
- Event Service
- Booking Service
- Payment Service
- Notification Service

Additional Technologies:

- Redis
- Kafka
- API Gateway
- Service Discovery
- Configuration Server

---

## Phase 3 — Production Readiness

The final phase focuses on scalability and deployment.

Topics include:

- Monitoring
- Logging
- CI/CD
- Docker Compose
- AWS Deployment
- Performance Optimization

---

# Project Structure

```
eventflow/
│
├── docs/
├── src/
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Documentation Structure

Every major topic has its own documentation.

| File | Description |
|------|-------------|
| 01-project-setup.md | Project goals and learning roadmap |
| 02-docker.md | Docker fundamentals and setup |
| 03-postgresql.md | PostgreSQL concepts and commands |
| 04-spring-boot.md | Spring Boot architecture |
| 05-jpa.md | JPA & Hibernate |
| 06-security.md | Spring Security & JWT |
| architecture.md | Overall system architecture |

---

# Development Philosophy

Every feature follows the same workflow.

```
Requirement
      ↓
Understand the problem
      ↓
Study the concept
      ↓
Design the solution
      ↓
Implement
      ↓
Review
      ↓
Refactor
```

The focus is on understanding *why* something is implemented rather than simply making it work.

---

# Long-Term Goal

By the completion of EventFlow, the project should demonstrate the ability to design and implement a production-grade backend system using modern Java backend technologies while documenting the reasoning behind every architectural decision.

The repository is intended to serve both as a learning resource and as a portfolio project that reflects professional backend development practices.