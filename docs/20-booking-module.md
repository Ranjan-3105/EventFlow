20 — Booking Module

Overview

The Booking Module is responsible for creating event seat reservations in EventFlow.

Booking is a concurrency-sensitive operation because multiple users may attempt to reserve the same seat simultaneously. The module combines Spring Security, PostgreSQL, Redis, and transactional persistence to provide controlled and consistent seat reservation.

Core Responsibilities

Authenticate the booking user through JWT

Validate the requested event and seats

Prevent duplicate seat selection

Verify that seats belong to the event's hall

Acquire distributed Redis locks for requested seats

Prevent concurrent reservation of the same seat

Verify persistent booking availability in PostgreSQL

Create Booking and BookingSeat records

Maintain temporary seat reservations using Redis TTL

Roll back partially acquired Redis locks when a booking attempt fails

Design Principle

Redis provides distributed concurrency control, while PostgreSQL remains the persistent source of truth for booking state.

1. Booking Domain

A booking represents a user's reservation for one or more seats for a specific event.

A Booking contains:

User

Event

Booking status

Seat count

Total amount

Creation timestamp

Update timestamp

Each selected seat is represented through a separate BookingSeat entity.

For example:

Booking
├── User: Rohan
├── Event: Avengers: Secret Wars
├── Status: PENDING
├── Seat Count: 2
└── Total Amount: ₹1000

BookingSeat
├── A3 → ₹500
└── A4 → ₹500

This separation allows a single booking to contain multiple seats while maintaining an individual record for every reserved seat.

2. API Endpoint

Create Booking

POST /api/bookings

Creates a booking for the authenticated user.

Authentication: Required

Authorization: Bearer <JWT>
Content-Type: application/json

Request

{
"eventId": "<EVENT_ID>",
"seatIds": [
"<SEAT_ID_1>",
"<SEAT_ID_2>"
]
}

The client does not provide a userId. The authenticated user is resolved from Spring Security's SecurityContext.

Successful Response

{
"bookingId": "<BOOKING_ID>",
"eventId": "<EVENT_ID>",
"status": "PENDING",
"seatCount": 2,
"totalAmount": 1000.00
}

3. Booking Creation Flow

The booking operation follows a deliberate sequence:

Request
│
▼
Authenticate User
│
▼
Load Event
│
▼
Validate Seat Structure
│
├── Invalid ──────────────► 400 Bad Request
│
▼
Acquire Redis Locks
│
├── Lock unavailable ─────► 409 Conflict
│
▼
Check PostgreSQL Availability
│
├── Already booked ───────► 409 Conflict
│
▼
Calculate Total Amount
│
▼
Create Booking
│
▼
Create BookingSeat Records
│
▼
Return BookingResponse

Redis locking intentionally happens before the PostgreSQL availability check so competing requests are filtered at the distributed lock layer before reaching the persistent booking check.

4. Authentication and User Resolution

The booking endpoint requires a valid JWT.

The JWT authentication filter extracts the user's identity and places the authenticated principal into Spring Security's SecurityContext.

BookingService then obtains the authenticated user's email:

SecurityContextHolder
.getContext()
.getAuthentication()
.getName();

The corresponding User entity is loaded from PostgreSQL.

The client therefore cannot select another user by supplying a different user ID.

5. Event Validation

The requested event is loaded using its UUID.

If the event does not exist, the booking operation fails with an EventNotFoundException.

Event UUID
│
▼
EventRepository.findById(...)
│
├── Found ──────► Continue
│
└── Not Found ──► EventNotFoundException

6. Seat Structural Validation

Before Redis locking, the service validates the structural correctness of the requested seats.

This validation includes:

Duplicate Seat IDs

The same seat cannot appear multiple times in a booking request.

Seat Existence

Every requested seat UUID must correspond to an existing Seat entity.

Seat-Hall Relationship

Every requested seat must belong to the hall associated with the requested event.

This prevents a client from attempting to book a seat belonging to another hall.

Structural validation does not determine whether a seat is currently booked. That responsibility is handled after Redis locking.

7. Redis Distributed Seat Locking

After structural validation, the service attempts to acquire a Redis lock for every requested seat.

The Redis key follows this format:

booking:lock:{eventId}:{seatId}

The Redis value contains a unique lock-owner UUID.

Example:

Key:
booking:lock:<eventId>:<seatId>

Value:
<lock-owner-uuid>

8. Atomic Lock Acquisition

Seat locks are acquired using Redis's atomic SET NX semantics with an expiration time.

Conceptually:

SET <key> <owner> NX EX <TTL>

NX means the key is created only when it does not already exist.

Therefore:

Seat Available
│
▼
Redis SET NX
│
▼
Lock Acquired

while:

Seat Already Locked
│
▼
Redis SET NX
│
▼
Operation Fails
│
▼
409 Conflict

This prevents two concurrent booking attempts from acquiring the same seat lock.

9. Lock Ownership

Every booking attempt generates a unique lock owner:

UUID.randomUUID().toString()

The generated UUID is stored as the Redis value.

The owner identity ensures that a booking attempt can release only the locks it acquired.

This prevents one booking attempt from accidentally deleting another booking attempt's Redis lock.

10. Lock TTL

Every Redis seat lock has a finite TTL.

The TTL prevents abandoned locks from remaining indefinitely if the application or client never completes the booking process.

Example:

Lock created
│
▼
TTL countdown
│
▼
TTL reaches 0
│
▼
Redis automatically removes the key

The current implementation uses a temporary reservation window of approximately 10 minutes.

The TTL can be verified directly through Redis:

docker exec -it eventflow-redis redis-cli TTL "booking:lock:<eventId>:<seatId>"

11. Multi-Seat Booking

A booking can contain multiple seats.

For example:

A3
A4

The service attempts to acquire a Redis lock for each seat.

A3 → Lock acquired
A4 → Lock acquired
│
▼
Continue booking creation

The booking proceeds only when every requested seat has been successfully locked.

12. Partial Lock Rollback

Multi-seat booking requires explicit cleanup when only some locks are acquired.

Consider:

A5 → Lock acquired
A3 → Lock unavailable

Without cleanup, A5 would remain locked even though the booking failed.

EventFlow therefore releases every lock acquired during the current attempt:

A5 → LOCKED
A3 → FAILED
│
▼
Release A5
│
▼
Reject booking

This behavior has been manually verified with Redis.

13. PostgreSQL Availability Check

Redis is the concurrency gate, but PostgreSQL remains the persistent source of truth for booking state.

After all requested Redis locks are successfully acquired, the service checks whether any requested seat already belongs to an active booking.

The active statuses currently considered are:

PENDING
CONFIRMED

If PostgreSQL reports an active booking, the Redis locks acquired by the current request are released before the exception is propagated.

14. Why Redis Comes Before PostgreSQL

The order of operations is intentional.

Without distributed locking, concurrent requests could all reach PostgreSQL at approximately the same time:

User A ──► PostgreSQL ──► A3 available
User B ──► PostgreSQL ──► A3 available

With Redis:

User A ──► Redis ──► A3 LOCKED ──► PostgreSQL
User B ──► Redis ──► A3 UNAVAILABLE ──► 409

Redis therefore acts as the fast concurrency gate, reducing contention on the persistent booking layer.

PostgreSQL still performs the final persistent availability validation.

15. Booking Creation

Once:

The user is authenticated

The event exists

The seats are structurally valid

All Redis locks are acquired

PostgreSQL confirms availability

the service creates a Booking.

The initial booking status is:

PENDING

The total amount is calculated as:

Event Base Price × Number of Seats

Example:

Base Price = ₹500
Seats      = 2

Total      = ₹1000

16. BookingSeat Creation

A BookingSeat entity is created for every selected seat.

Example:

Booking
│
├── BookingSeat → A3 → ₹500
└── BookingSeat → A4 → ₹500

The price paid for each seat is stored with the BookingSeat record.

This preserves the price associated with the booking even if the event's current base price changes later.

17. Transaction Management

BookingService uses Spring's:

@Transactional

The database persistence operations therefore execute inside a transaction.

The database portion of the flow is:

Begin Transaction
│
├── Create Booking
│
└── Create BookingSeat records
│
▼
Commit

If a runtime exception occurs after Redis locks have been acquired, the service explicitly releases those Redis locks.

Redis cleanup and database transaction management are therefore handled separately:

PostgreSQL
→ Transactional persistence

Redis
→ Explicit distributed lock lifecycle

18. Lock Lifecycle

A successfully created PENDING booking intentionally retains its Redis locks.

The intended lifecycle is:

PENDING
│
├── Payment succeeds
│       │
│       ▼
│   CONFIRMED
│
└── Reservation expires
│
▼
EXPIRED

The current booking module establishes the PENDING state and Redis reservation mechanism.

Payment processing, booking confirmation, and expiration handling are implemented as subsequent modules.

19. Error Handling

Event Not Found

404 Not Found

Example:

{
"message": "Event not found"
}

Invalid Booking

400 Bad Request

Possible causes:

Duplicate seat IDs

Non-existent seat

Seat does not belong to the event's hall

Seat Temporarily Unavailable

409 Conflict

Example:

{
"message": "Seat A3 is temporarily unavailable."
}

This indicates that Redis could not acquire the requested seat lock.

Seat Already Booked

409 Conflict

Example:

{
"message": "Seat A3 is already booked."
}

This indicates that PostgreSQL contains an active booking for the requested seat.

20. Verified Scenarios

The current implementation has been manually verified for:

Successful multi-seat booking

Redis lock creation

Redis TTL

Concurrent booking rejection

PostgreSQL booking persistence

PostgreSQL BookingSeat persistence

Correct total amount calculation

Partial Redis-lock rollback

Successful Multi-Seat Booking

A3 + A4
│
▼
Redis locks acquired
│
▼
PostgreSQL availability confirmed
│
▼
Booking created
│
▼
PENDING
│
▼
₹1000 total

Concurrent Booking

User A
│
└── A3 + A4
│
└── Redis locks acquired

User B
│
└── A3 + A4
│
└── Redis lock rejected
│
▼
409 Conflict

Verified response:

Seat A3 is temporarily unavailable.

Partial Lock Failure

A5 → Lock acquired
A3 → Lock unavailable
│
▼
A5 → Lock released
│
▼
Booking rejected

The failed request does not leave an A5 Redis lock behind.

21. Architecture

                         Client
                           │
                           ▼
                    Spring Security
                           │
                           ▼
                  BookingController
                           │
                           ▼
                    BookingService
                      /                               /                                ▼              ▼
                Redis          PostgreSQL
             Seat Locks       Booking State
                 │                  │
                 │                  ├── Booking
                 │                  └── BookingSeat
                 │
                 └── TTL

Component Responsibilities

Component

Responsibility

BookingController

HTTP request and response handling

BookingService

Booking business logic and orchestration

SeatLockService

Redis lock acquisition and release

BookingRepository

Booking persistence

BookingSeatRepository

Booking-seat persistence

Redis

Distributed seat locking and TTL

PostgreSQL

Persistent booking state

Spring Security

JWT authentication

22. Current Status

Feature

Status

User Authentication

✅

Event Validation

✅

Seat Structural Validation

✅

Multi-Seat Booking

✅

PostgreSQL Persistence

✅

Redis Integration

✅

Redis Seat Locking

✅

Lock Ownership

✅

Lock TTL

✅

Partial Lock Rollback

✅

PENDING Booking

✅

Concurrent Booking Protection

✅

Booking Expiration

🚧

Payment Processing

🚧

CONFIRMED Booking

🚧

Summary

The Booking Module implements concurrency-aware seat reservation by combining Redis distributed locks with PostgreSQL persistence.

The responsibilities are intentionally separated:

Redis
→ Controls temporary ownership of seats during concurrent booking attempts.

PostgreSQL
→ Stores the durable booking state.

BookingService
→ Coordinates validation, locking, availability checks, and persistence.

This design prevents concurrent users from acquiring the same seat, supports multi-seat reservations, cleans up partial lock acquisition failures, and provides automatic lock recovery through TTL.

The next stage of the booking lifecycle is to implement reservation expiration, payment processing, and transition from PENDING to CONFIRMED.