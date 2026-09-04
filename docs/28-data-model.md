# 28. Final Data Model

## Concept

EventFlow uses a relational database (PostgreSQL) to enforce strict relationships and data integrity. The data model is normalized to prevent duplication and ensure that business rules are mathematically sound at the storage layer.

## Entity Overview

### 1. Security & Identity
- **`User`:** Represents an authenticated person (Customer, Organizer, Admin).
- **`Role`:** Represents a set of permissions (mapped Many-to-Many with User).

### 2. Infrastructure (The "Where")
- **`Venue`:** A physical building or location (e.g., "PVR Cinemas").
- **`Hall`:** A specific room inside a Venue (e.g., "Screen 1"). A Venue has many Halls.
- **`Seat`:** A specific physical chair inside a Hall. A Hall has many Seats. 
  - *Note:* The `Seat` entity represents the physical chair, *not* a ticket. It does not know about events or bookings.

### 3. Events (The "What")
- **`Event`:** A specific movie screening or concert happening at a specific time in a specific Hall.
  - Relates to `Hall`.
  - Has an `EventStatus` (e.g., `DRAFT`, `PUBLISHED`).

### 4. Transactions (The "Who & How Much")
- **`Booking`:** Represents a user's intent to reserve seats for an Event.
  - Relates to `User` and `Event`.
  - Has a `BookingStatus` (`PENDING`, `CONFIRMED`, `EXPIRED`, `CANCELLED`).
- **`BookingSeat`:** A join table representing which specific `Seat`s are attached to which `Booking`.
  - Because an Event can have many Bookings, and a Booking can have many Seats, this prevents double-booking at the database level.
- **`Payment`:** Represents a financial transaction tied to a Booking.
  - Relates 1-to-1 with a `Booking`.
  - Has a `PaymentStatus` (`PENDING`, `SUCCESS`, `FAILED`).
  - Stores Razorpay order IDs and payment IDs for auditability.

## Entity Relationship Diagram

```text
+-----------+       +-----------+
|   Role    |       |   User    |
+-----------+       +-----------+
      |                   | 1
      | M                 |
      +----- (uses) ------+
                          | 1
                          |
                          | M
                    +-----------+
                    |  Booking  | 1 ------ 1 +-----------+
                    +-----------+            |  Payment  |
                      1 |                    +-----------+
                        |
                        | M
                  +-------------+
                  | BookingSeat |
                  +-------------+
                        | M
                        |
                      1 |
+-----------+       +-----------+
|   Event   | 1 --- |   Seat    |
+-----------+       +-----------+
      | M                 | M
      |                   |
    1 |                 1 |
+-----------+       +-----------+
|   Hall    | 1 --- |   Venue   |
+-----------+       +-----------+
```

*(Note: The diagram above is a conceptual simplification. In the actual JPA entities, `Seat` relates to `Hall`, not directly to `Venue`. `Event` relates to `Hall`, not directly to `Venue` either. The `BookingSeat` connects a `Booking` and a `Seat`.)*

### Corrected Relational Chain:
- `Venue` → (1:M) → `Hall`
- `Hall` → (1:M) → `Seat`
- `Hall` → (1:M) → `Event`
- `Event` → (1:M) → `Booking`
- `User` → (1:M) → `Booking`
- `Booking` → (1:M) → `BookingSeat`
- `Seat` → (1:M) → `BookingSeat`
- `Booking` → (1:1) → `Payment`
