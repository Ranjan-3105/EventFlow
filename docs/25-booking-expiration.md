# 25. Booking Expiration

## Concept: Reclaiming Abandoned Bookings

When a user selects seats and clicks "Book", the backend creates a `PENDING` booking and locks those seats in Redis for 10 minutes. 

But what if the user closes their browser and never completes the payment?
1. The Redis lock will automatically expire after 10 minutes, making the seats available for others.
2. However, the PostgreSQL database still has a `PENDING` booking sitting there forever. 

Over time, these abandoned `PENDING` bookings would clutter the database and make revenue/booking reports inaccurate. We need a cleanup mechanism to transition these abandoned bookings to an `EXPIRED` state.

## Business Rules

1. A Booking has an `expiresAt` timestamp (calculated as `createdAt + 10 minutes`).
2. If `now() > expiresAt` and the status is still `PENDING`, the booking is considered abandoned.
3. Abandoned bookings must transition to `EXPIRED`.
4. `CONFIRMED` or `CANCELLED` bookings are not subject to expiration.

## Implementation: `BookingExpirationService`

We use Spring's `@Scheduled` annotation to run a background job that periodically sweeps the database for expired bookings.

```java
// BookingExpirationService.java
@Service
public class BookingExpirationService {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void expireBookings() {

        Instant now = Instant.now();

        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndExpiresAtLessThanEqual(
                        BookingStatus.PENDING,
                        now
                );

        if (expiredBookings.isEmpty()) return;

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.setUpdatedAt(now);
        }

        bookingRepository.saveAll(expiredBookings);
    }
}
```

### The Polling Interval

The `@Scheduled(fixedDelay = 5000)` annotation tells Spring to run this method every 5 seconds (5000 milliseconds) after the previous execution finishes.

### The Lifecycle Flow

```text
       [ CREATE BOOKING ]
               |
               ↓
          (PENDING)
               |
      +--------+--------+
      |                 |
(Payment Success)  (Time > expiresAt)
      |                 |
      ↓                 ↓
 (CONFIRMED)        (EXPIRED)
```

## Redis Lock TTL vs. PostgreSQL Expiration

It is important to distinguish between these two concepts:

1. **Redis Lock TTL (10 minutes):** A fast, in-memory mechanism that strictly enforces the physical unavailability of a seat during checkout. It expires automatically within Redis.
2. **PostgreSQL Expiration:** A background task (`BookingExpirationService`) that periodically reconciles the durable database state to match the reality that the payment window has closed.

Because Redis expires locks atomically, we don't have to worry about a race condition where a seat is locked in PostgreSQL but the background job hasn't run yet. The source of truth for "is this seat currently being checked out by someone else?" is always Redis. The source of truth for "what is the historical state of this user's booking intent?" is PostgreSQL.

## Real-World Reliability Observation

During development, an interesting scenario was observed:
A frontend glitch caused a user to start the booking process (locking seats and creating a `PENDING` booking), but they were unexpectedly redirected due to a token issue, preventing them from paying.

Because of this architecture, the system cleanly healed itself without any manual intervention:
1. The 10-minute Redis lock expired automatically, releasing the seats for other users.
2. The `BookingExpirationService` swept the database and marked the abandoned booking as `EXPIRED`.
3. The system returned to a completely consistent state.

This demonstrates the resilience of separating temporary coordination (Redis) from durable state (PostgreSQL).
