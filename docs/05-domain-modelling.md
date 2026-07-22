# Domain Modeling - EventFlow

## Objective

The first step in designing a scalable backend is identifying the core business entities before writing any code.

Instead of directly creating database tables, we modeled the real-world objects involved in an event ticket booking platform and established their responsibilities and relationships.

---

# Design Philosophy

Every entity should have **one responsibility**.

Avoid storing data that belongs to another entity or data that can be derived.

Examples:

❌ User contains bookedTicketIds

✔ Booking owns ticket reservations

---

❌ Store Age

✔ Store Date of Birth

Age is a derived attribute.

---

❌ Store Number Of Seats

✔ Count Seat records

Number of seats is derived from the Seat table.

---

# Identified Core Entities

## User

Represents a registered customer of the platform.

Responsibilities:

- Authentication
- Personal information
- Identity

Current Fields:

- UUID id
- firstName
- lastName
- email
- phoneNumber
- password
- role
- enabled
- createdAt
- updatedAt

---

## Venue

Represents a physical location.

Example:

PVR Bhubaneswar

Responsibilities:

- Name
- Address
- City

A venue does NOT know:

- Movies
- Bookings
- Payments

---

## Screen (Audi)

A venue contains multiple screens.

Example:

Audi 1

Audi 2

IMAX

Responsibilities:

- Screen identity
- Physical auditorium

A screen does NOT know:

- Current movie
- Payments
- Bookings

---

## Seat

Seats belong to Screens.

A seat is a physical object.

Examples:

A1

A2

B15

Seats exist before events.

---

## Event

Represents the actual movie/show.

Examples:

Interstellar

Oppenheimer

Concert

Stand-up Comedy

An event is NOT tied to a venue.

The same event can happen in multiple venues.

---

## Event Schedule

Represents one occurrence of an event.

Example:

Interstellar

22 July

7 PM

Audi 2

This is where an event gets assigned to:

- Venue
- Screen
- Date
- Time

---

## Booking

Created when a user reserves seats.

Booking represents the reservation process.

Possible states:

- Pending
- Confirmed
- Cancelled
- Failed

Booking exists before Ticket generation.

---

## Payment

Responsible only for payment processing.

Possible states:

- Pending
- Success
- Failed

Ticket generation only happens after successful payment.

---

## Ticket

Generated only after:

Booking
+
Successful Payment

The ticket acts as proof of reservation.

---

# Important Design Decisions

## Why UUID instead of Long?

UUID allows independent ID generation across distributed systems.

Advantages:

- Suitable for microservices
- Globally unique
- No central ID generator required

Trade-off:

- Larger indexes
- Slightly slower than Long

---

## Why Booking exists?

Booking separates:

Seat Reservation

from

Ticket Generation

Flow:

User

↓

Booking Created

↓

Payment

↓

Ticket Generated

If payment fails:

Booking remains

No ticket is generated.

---

## Why Seats belong to Screens?

Seats are physical objects.

Events use Screens.

Events do not create Seats.

Venue

↓

Screen

↓

Seat

↓

Event uses Screen

---

## Derived Attributes

Never store data that can be calculated.

Examples:

Store:

- Date Of Birth

Calculate:

- Age

Store:

- Seat records

Calculate:

- Number of Seats

---

## Future Relationships

Venue

↓

Screens

↓

Seats

↓

Event Schedule

↓

Booking

↓

Payment

↓

Ticket

↓

User