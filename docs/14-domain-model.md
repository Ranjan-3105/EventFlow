# 13. Domain Model

# Overview

A domain model represents the real-world objects that exist inside an application and the relationships between them.

In EventFlow, every feature revolves around a few core entities.

```
User

↓

Venue

↓

Hall

↓

Event

↓

Seat

↓

Booking

↓

Payment

↓

Ticket
```

As the application grows, these entities form the backbone of the database.

---

# What is an Entity?

An Entity is a Java class that represents a table in the database.

Example

```java
@Entity
@Table(name = "venues")
public class Venue {
    ...
}
```

becomes

```
Database

↓

venues
```

Each object of that class represents one row in the table.

---

# Current Domain Model

```
User

ADMIN
ORGANIZER
USER

        │
        ▼

Venue

        │
        ▼

Hall

        │
        ▼

Event
```

Future versions will extend this into a complete ticket booking platform.

---

# User

Purpose

Represents every person using the platform.

Current Roles

```
ADMIN

ORGANIZER

USER
```

Responsibilities

ADMIN

- Create venues
- Create halls

ORGANIZER

- Create events

USER

- Book tickets (future)

---

Important Fields

```
id

firstName

lastName

email

password

phoneNumber

role

enabled

createdAt

updatedAt
```

Business Rules

- Email must be unique.
- Password is stored in encrypted form.
- Every user has exactly one role.
- Authentication is performed using email and password.

---

# Venue

Purpose

Represents a physical building where events can be hosted.

Example

```
PVR Nexus Mall

INOX Esplanade

KIIT Auditorium
```

A venue contains one or more halls.

---

Important Fields

```
id

name

address

city

state

country

latitude

longitude

createdAt

updatedAt
```

Business Rules

- Name is required.
- Address is required.
- Geographic coordinates are stored.
- Multiple venues may have the same name if they exist in different locations.

---

Relationship

```
Venue

↓

Many Halls
```

JPA

```java
@OneToMany(mappedBy = "venue")
private List<Hall> halls;
```

---

# Hall

Purpose

Represents an auditorium inside a venue.

Example

```
PVR Nexus Mall

├── Hall 1

├── Hall 2

├── Hall 3
```

A hall belongs to exactly one venue.

---

Important Fields

```
id

hallNumber

capacity

venue

createdAt

updatedAt
```

Business Rules

- Hall number must be unique within a venue.
- Capacity must be greater than zero.
- Hall must belong to an existing venue.

---

Relationship

```
Many Halls

↓

One Venue
```

JPA

```java
@ManyToOne
@JoinColumn(name = "venueId")
private Venue venue;
```

---

Future Relationship

```
Hall

↓

Many Seats

↓

Many Events
```

---

# Event

Purpose

Represents an event hosted by an organizer.

Examples

```
Spring Boot Workshop

Tech Conference

Music Concert
```

An event temporarily reserves a hall for a specific time interval.

---

Important Fields

```
id

title

description

bannerUrl

startTime

endTime

status

hall

organizer

createdAt

updatedAt
```

Business Rules

- Organizer must exist.
- Hall must exist.
- Start time must be before end time.
- Hall cannot be booked twice for overlapping time intervals.

---

Relationships

```
Organizer

↓

Many Events
```

```
Hall

↓

Many Events
```

---

# Future Entities

The following entities will complete the booking system.

---

## Seat

Represents a physical seat inside a hall.

Examples

```
A1

A2

B5

C10
```

Relationship

```
Hall

↓

Many Seats
```

---

## Booking

Represents a reservation made by a user.

Relationship

```
User

↓

Many Bookings
```

```
Booking

↓

One Event
```

```
Booking

↓

Many Seats
```

---

## Payment

Represents the payment for a booking.

Relationship

```
Booking

↓

One Payment
```

---

## Ticket

Represents the final ticket issued after successful payment.

Relationship

```
Booking

↓

One Ticket
```

---

# Entity Relationships

Current

```
User

│

├───────────────┐

│               │

▼               ▼

Venue         Event

│               ▲

▼               │

Hall────────────┘
```

Future

```
Venue

↓

Hall

↓

Seat

↓

Booking

↓

Payment

↓

Ticket
```

---

# Why Normalize the Database?

Instead of storing everything inside one table

```
Event

Hall Name

Venue Name

Seat Number

User

Payment
```

the information is separated into independent entities.

Advantages

- No duplicate data
- Easier updates
- Better consistency
- Better scalability
- Easier relationships

---

# Why UUID?

EventFlow uses UUID instead of auto-increment IDs.

Example

```
4d415308-9798-46be-b47d-f27f6c39b32a
```

Advantages

- Globally unique
- Harder to guess
- Better for distributed systems
- Easier database merging

---

# Current Database Structure

```
User

↓

Venue

↓

Hall

↓

Event
```

Upcoming

```
User

↓

Venue

↓

Hall

↓

Seat

↓

Event

↓

Booking

↓

Payment

↓

Ticket
```

---

# Summary

Current entities

- User
- Venue
- Hall
- Event

Upcoming entities

- Seat
- Booking
- Payment
- Ticket

The domain model provides the foundation of EventFlow. Every API, service, repository, and business rule revolves around these entities and their relationships. As more modules are added, the same model will continue to evolve without changing its core design.