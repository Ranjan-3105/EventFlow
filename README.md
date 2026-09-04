# 🎟️ EventFlow

> A production-inspired full-stack Event Ticket Booking System.

EventFlow is a comprehensive event booking platform that handles user authentication, event and venue management, dynamic seat selection, secure payment processing via Razorpay, and asynchronous email notifications using Kafka.

The project documents the complete journey from monolithic development to a scalable, distributed architecture.

---

## ✨ Key Features

- **Authentication & Security:** Secure JWT-based stateless authentication, role-based access control (Admin, Organizer, User), and BCrypt password hashing.
- **Venue & Event Management:** Robust administrative tools for creating venues, configuring custom hall seating layouts, and publishing events with dynamic pricing.
- **Seat Booking System:** Interactive frontend seat map, concurrent locking using Redis, and real-time validation.
- **Payment Integration:** Secure checkout via Razorpay, including server-side signature verification.
- **Asynchronous Notifications:** Kafka event streaming to send decoupled booking confirmation emails via Brevo SMTP.
- **Modern User Interface:** A responsive, premium frontend built with React, Tailwind CSS, and Shadcn UI.

---

## 🏗️ Architecture

EventFlow is currently implemented as a monolithic backend with decoupled infrastructure services, and a standalone single-page application frontend.

```text
       React Frontend (Vite)
                │
                ▼
        Spring Boot Backend
                │
    ┌───────────┼───────────┐
    ▼           ▼           ▼
PostgreSQL    Redis       Kafka
(Primary)   (Caching) (Event Stream)
```

**Core Backend Layers:**
- Controller (API routing, request validation)
- Service (Business logic, transaction management)
- Repository (Spring Data JPA)
- Domain / Entity (Hibernate ORM)

---

## 🛠️ Tech Stack

### Frontend
- **Framework:** React 19 + TypeScript + Vite
- **Styling:** Tailwind CSS + Shadcn UI
- **Routing:** React Router
- **HTTP Client:** Axios

### Backend
- **Framework:** Spring Boot (Java 21)
- **Security:** Spring Security + JWT
- **Database:** PostgreSQL + Hibernate / Spring Data JPA
- **Caching & Locks:** Redis
- **Messaging:** Apache Kafka
- **Payments:** Razorpay Java SDK
- **Mailing:** JavaMailSender (Brevo SMTP)

---

## 🔄 Core Booking Flow

1. **User Auth:** User registers and logs in, receiving a JWT token.
2. **Event Selection:** User browses available events and selects a showtime.
3. **Seat Selection:** User views the hall layout and selects available seats.
4. **Checkout Initialization:** The frontend initiates a booking. The backend locks the requested seats in PostgreSQL/Redis.
5. **Payment Processing:** A Razorpay Order is created. The user pays via the Razorpay checkout UI.
6. **Payment Verification:** Razorpay calls the frontend with a signature. The frontend forwards this to the backend for cryptographic verification.
7. **Confirmation & Notification:** The backend marks the booking as `CONFIRMED` and publishes a `booking.confirmed` event to Kafka.
8. **Email Delivery:** The Kafka consumer reads the event and sends a confirmation email to the user.

---

## 📂 Repository Structure

```text
EventFlow/
├── Backend/                 # Spring Boot application source code
│   ├── src/main/java/       # Java source files (controllers, services, entities)
│   ├── src/main/resources/  # Application properties and configs
│   ├── pom.xml              # Maven dependencies
│   └── docker-compose.yml   # Infrastructure definitions (Postgres, Redis, Kafka)
│
├── frontend/                # React + Vite application
│   ├── src/                 # React components, pages, context, services
│   ├── tailwind.config.js   # Tailwind configuration
│   └── package.json         # Node dependencies
│
└── docs/                    # Project documentation
    └── SETUP.md             # Installation and startup guide
```

---

## 🚀 Quick Start

To run the project locally, you will need Java 21, Node.js 20+, and Docker installed.

Detailed step-by-step instructions for environment configuration and startup are available in the **[Setup Guide](docs/SETUP.md)**.

### Summary
1. Copy `.env.example` to `.env` in both `Backend` and `frontend`.
2. Start infrastructure: `cd Backend && docker-compose up -d`
3. Start backend: `.\mvnw spring-boot:run`
4. Start frontend: `cd frontend && npm install && npm run dev`

---

## 📚 API Documentation

Currently, API contracts are strictly defined and documented within the Spring Boot `Controller` and `DTO` layers. A high-level overview of the final endpoints is available in **[27-api-inventory.md](docs/27-api-inventory.md)**. Inspect the classes located in `Backend/src/main/java/com/eventflow/eventflow/controller/` for detailed request and response payloads.