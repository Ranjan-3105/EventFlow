# 15. Query Derivation vs JPQL

# Overview

Spring Data JPA provides multiple ways to query the database.

The two approaches used in EventFlow are:

- Query Derivation
- JPQL (Java Persistence Query Language)

Both achieve the same goal—retrieving data from the database—but are suited for different situations.

---

# What is Query Derivation?

Query Derivation is a feature of Spring Data JPA where the framework automatically generates SQL queries from repository method names.

Instead of writing SQL manually, we describe the query through the method name.

Example

```java
boolean existsByVenueIdAndHallNumber(
        UUID venueId,
        int hallNumber
);
```

No SQL.

No JPQL.

Spring automatically generates the appropriate query.

Conceptually, Hibernate executes something similar to

```sql
SELECT EXISTS (
    SELECT 1
    FROM halls
    WHERE venue_id = ?
      AND hall_number = ?
);
```

The developer never writes this SQL.

---

# How Does Spring Understand This?

Consider the method

```java
existsByVenueIdAndHallNumber(...)
```

Spring breaks it into pieces.

```
exists

↓

By

↓

VenueId

↓

And

↓

HallNumber
```

Each keyword has a meaning.

```
exists

↓

Return boolean
```

```
By

↓

Start filtering
```

```
VenueId

↓

WHERE venue_id = ?
```

```
And

↓

AND
```

```
HallNumber

↓

hall_number = ?
```

Spring then builds the final query automatically.

---

# Advantages

Very little code.

Easy to read.

Less error-prone.

No SQL knowledge required for simple queries.

Ideal for CRUD operations.

---

# Limitations

Method names can become very long.

Example

```java
findByVenueIdAndCapacityGreaterThanAndCityAndCreatedAtAfter(...)
```

Difficult to read.

Not suitable for complex business logic.

---

# Query Derivation Keywords

Common keywords supported by Spring Data JPA

```
findBy

existsBy

countBy

deleteBy
```

Conditions

```
And

Or

Between

After

Before

GreaterThan

LessThan

Containing

Like

OrderBy
```

Spring combines these to generate queries.

---

# EventFlow Example

HallRepository

```java
boolean existsByVenueIdAndHallNumber(
        UUID venueId,
        int hallNumber
);
```

Purpose

Before creating a hall, ensure that another hall with the same number does not already exist in the venue.

Business Rule

```
Venue

↓

Hall 1

Hall 2

Hall 3
```

Adding another

```
Hall 1
```

should fail.

This query perfectly matches the business requirement.

---

# What is JPQL?

JPQL stands for

**Java Persistence Query Language**

Unlike SQL,

JPQL works with **Entities**, not database tables.

Example

```java
@Query("""
SELECT COUNT(e) > 0
FROM Event e
WHERE e.hall.id = :hallId
AND (
    e.startTime < :endTime
    AND e.endTime > :startTime
)
""")
boolean existsConflictingEvent(
        UUID hallId,
        Instant startTime,
        Instant endTime
);
```

Notice

```
Event

Hall

startTime
```

These are Java entity names.

Not table names.

---

# Why Not Use Query Derivation Here?

Imagine writing this using method names.

```
existsByHallIdAndStartTimeBeforeAndEndTimeAfter(...)
```

Still incomplete.

We also need

```
(

A

AND

B

)
```

The overlap condition becomes difficult to express.

The business rule is more important than the method name.

JPQL makes it much easier to understand.

---

# Event Conflict Logic

Suppose an event already exists.

```
10:00

↓

13:00
```

Now another organizer requests

```
11:00

↓

14:00
```

JPQL checks

```
Existing Start < New End

AND

Existing End > New Start
```

If both conditions are true

↓

The events overlap.

↓

Return

```
true
```

↓

Throw

```
HallAlreadyBookedException
```

---

# SQL vs JPQL

SQL

```sql
SELECT *
FROM events
WHERE hall_id = ?
```

JPQL

```java
SELECT e
FROM Event e
WHERE e.hall.id = :hallId
```

SQL uses

- Tables
- Columns

JPQL uses

- Entities
- Fields

Hibernate converts JPQL into SQL automatically.

---

# Named Parameters

Instead of

```java
?
```

JPQL supports named parameters.

Example

```java
:hallId

:startTime

:endTime
```

These values are supplied by the repository method.

```java
existsConflictingEvent(
        UUID hallId,
        Instant startTime,
        Instant endTime
)
```

This improves readability.

---

# Query Derivation vs JPQL

| Query Derivation | JPQL |
|------------------|------|
| Generated automatically | Written manually |
| Best for simple queries | Best for complex queries |
| Easy to read | More flexible |
| Less code | More control |
| Limited expressiveness | Supports complex conditions |

---

# When to Use Which?

Use Query Derivation when

- Searching by ID
- Checking existence
- Simple filters
- Counting records
- Basic CRUD operations

Examples

```java
findByEmail(...)

existsByVenueIdAndHallNumber(...)

countByStatus(...)
```

---

Use JPQL when

- Multiple conditions
- Joins
- Complex business rules
- Aggregations
- Time interval checks
- Custom projections

Example

```java
existsConflictingEvent(...)
```

---

# EventFlow Decisions

HallRepository

```
Query Derivation
```

Reason

Simple existence check.

---

EventRepository

```
JPQL
```

Reason

Complex overlap detection.

Writing this as a derived method would be difficult and less readable.

---

# Best Practices

✔ Prefer Query Derivation for simple queries.

✔ Switch to JPQL when the method name becomes difficult to read.

✔ Write JPQL using entity names, not table names.

✔ Keep repository methods focused on database access.

✔ Keep business decisions inside the Service layer.

---

# Summary

Spring Data JPA offers multiple ways to access the database.

For simple lookups,

```
Query Derivation
```

is concise and expressive.

For advanced business rules,

```
JPQL
```

provides greater flexibility.

EventFlow intentionally demonstrates both approaches:

- Query Derivation for Hall validation.
- JPQL for Event scheduling conflicts.

Understanding when to use each is an important step toward writing clean, maintainable Spring applications.