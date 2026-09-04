# 23. Redis: Caching vs. Locking

## Concept: Two Distinct Roles

In EventFlow, **Redis** is used heavily, but it serves **two entirely different responsibilities**. It is critical not to confuse them.

1. **Seat Locking (Coordination):** Temporary locks to prevent double-booking during the checkout flow.
2. **Event Caching (Performance):** Storing serialized representations of published events to speed up read-heavy API endpoints.

## 1. Seat Locking (`SeatLockService`)

When a user selects seats and proceeds to checkout, we acquire a distributed lock in Redis for those specific seats. 

- **Key:** `booking:lock:{eventId}:{seatId}`
- **Value:** Lock owner (e.g., the user's ID or email)
- **TTL:** 10 minutes (`LOCK_DURATION`)

If another user tries to book the same seat, `setIfAbsent` will return false, and they will be stopped immediately without touching PostgreSQL.

If the user successfully pays, we delete the lock. If they abandon the checkout, the TTL automatically expires the lock in Redis after 10 minutes, making the seat available again.

## 2. Event Caching (`EventCacheService`)

EventFlow is a read-heavy application. Users frequently browse the list of published events on the home page. Querying the database every time is inefficient. We use Redis as an in-memory cache to serve these requests rapidly.

### Cache-Aside Strategy

We implement the standard **Cache-Aside** (or lazy-loading) strategy for the `GET /api/events` endpoint.

```text
Request: GET /api/events
        |
        | 1. Check Redis (cache:events:published)
        |
   +----|----+
   |         |
[ HIT ]   [ MISS ]
   |         |
   |         | 2. Query PostgreSQL
   |         | 3. Map to EventResponse list
   |         | 4. Serialize to JSON
   |         | 5. Store in Redis (TTL: 5 mins)
   |         |
   +----|----+
        |
        | 6. Return JSON response
```

### Implementation Details

The `EventCacheService` relies on Spring Data Redis and Jackson (for JSON serialization):

```java
// EventCacheService.java

public List<EventResponse> getPublishedEvents() {
    String cached = redisTemplate.opsForValue().get("cache:events:published");
    if (cached == null) return null; // MISS
    
    // HIT: Deserialize and return
    return objectMapper.readValue(cached, new TypeReference<List<EventResponse>>() {});
}

public void cachePublishedEvents(List<EventResponse> events) {
    String json = objectMapper.writeValueAsString(events);
    redisTemplate.opsForValue().set(
            "cache:events:published", 
            json, 
            Duration.ofMinutes(5) // TTL
    );
}
```

### Cache Invalidation

When an organizer publishes a new event, the cache immediately becomes stale. To ensure users see the newly published event without waiting for the 5-minute TTL to expire, we actively invalidate (delete) the cache.

```java
// EventCacheService.java
public void invalidatePublishedEvents() {
    redisTemplate.delete("cache:events:published");
}
```
This is called from `EventService.publishEvent()`. The next read request will experience a cache MISS and re-populate the cache with the updated list of events.

**Note:** We currently only cache the main event listing (`/api/events`). Individual event details and seat availability are intentionally *not* cached, as seat availability is highly dynamic and must always reflect real-time accuracy.
