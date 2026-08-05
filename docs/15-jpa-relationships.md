# 14. JPA Relationships

# Overview

Real-world objects rarely exist in isolation.

For example,

```
Venue

↓

Hall

↓

Seat
```

A hall cannot exist without a venue.

Likewise,

```
Organizer

↓

Event
```

An event always belongs to an organizer.

JPA Relationships allow us to represent these real-world connections inside the database.

---

# Why Relationships?

Imagine storing everything inside a single table.

```
Event

Venue Name

Hall Number

Organizer Name

Seat Number

...
```

Problems

- Duplicate data
- Difficult updates
- Wasted storage
- Inconsistent information

Instead, the data is separated into different tables and linked together.

```
Venue

↓

Hall

↓

Event
```

This is known as **Database Normalization**.

---

# Types of Relationships

JPA supports four relationship types.

```
@OneToOne

@OneToMany

@ManyToOne

@ManyToMany
```

EventFlow currently uses

```
@OneToMany

@ManyToOne
```

---

# One-to-Many

Meaning

One object owns multiple objects.

Example

```
Venue

↓

Hall 1

Hall 2

Hall 3
```

One venue

↓

Many halls

JPA

```java
@OneToMany(mappedBy = "venue")
private List<Hall> halls;
```

---

# Many-to-One

Meaning

Many objects belong to one parent.

Example

```
Hall 1

↓

Venue

Hall 2

↓

Venue

Hall 3

↓

Venue
```

JPA

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "venueId")
private Venue venue;
```

This is exactly how Hall is implemented in EventFlow.

---

# JoinColumn

Consider these tables.

Venue

```
id

name
```

Hall

```
id

hallNumber

venueId
```

The column

```
venueId
```

acts as the foreign key.

JPA

```java
@JoinColumn(name = "venueId")
private Venue venue;
```

This tells Hibernate

> Store the Venue ID inside the Hall table.

---

# Foreign Key

Example

Venue

| id | name |
|----|------|
| 101 | PVR Nexus |

Hall

| id | hallNumber | venueId |
|----|------------|---------|
| 1 | Hall 1 | 101 |
| 2 | Hall 2 | 101 |

Notice

The Hall table does not duplicate the venue information.

It simply stores

```
venueId
```

---

# mappedBy

Suppose both classes contain references.

Venue

```java
@OneToMany(mappedBy = "venue")
private List<Hall> halls;
```

Hall

```java
@ManyToOne
private Venue venue;
```

Question

Who owns the relationship?

Answer

```
Hall
```

because it contains the foreign key.

Therefore,

```
mappedBy = "venue"
```

tells Hibernate

> The relationship is already managed by the `venue` field inside Hall.

Without `mappedBy`, Hibernate creates an unnecessary join table.

---

# Fetch Types

JPA has two loading strategies.

```
EAGER

LAZY
```

---

## EAGER Loading

```
Load Hall

↓

Automatically load Venue
```

Everything is fetched immediately.

Example

```
Hall

↓

Venue
```

Advantages

- Easy to use

Disadvantages

- More database queries
- Slower performance
- Higher memory usage

---

## LAZY Loading

```
Load Hall

↓

Only Hall
```

Venue is fetched only when needed.

Example

```java
hall.getVenue();
```

Only then does Hibernate execute another query.

Advantages

- Faster
- Less memory
- Better scalability

EventFlow uses

```java
@ManyToOne(fetch = FetchType.LAZY)
```

because production systems usually prefer lazy loading.

---

# Cascade Operations

Sometimes we want operations on the parent to affect the child.

Example

```
Venue

↓

Hall
```

Delete Venue

↓

Delete Halls

This is called **Cascade**.

Example

```java
@OneToMany(
    cascade = CascadeType.ALL
)
```

Cascade Types

```
PERSIST

MERGE

REMOVE

REFRESH

DETACH

ALL
```

Current EventFlow intentionally avoids cascade operations while learning.

Everything is saved explicitly inside the Service Layer.

This makes the application easier to understand.

---

# orphanRemoval

Imagine

```
Venue

↓

Hall 1

Hall 2

Hall 3
```

Removing

```
Hall 2
```

from the list.

Should it also disappear from the database?

If

```java
orphanRemoval = true
```

then yes.

Otherwise,

it only disappears from the Java collection.

---

# Bidirectional Relationship

Example

Venue

```java
private List<Hall> halls;
```

Hall

```java
private Venue venue;
```

Both classes know about each other.

Advantages

- Easy navigation

Disadvantages

- Infinite JSON recursion
- More complex mapping
- Harder debugging

---

# Unidirectional Relationship

Example

Hall

```java
private Venue venue;
```

Venue

No reference to Hall.

Advantages

- Simpler
- Easier APIs
- Easier debugging

Current EventFlow mostly uses **unidirectional relationships** because they are sufficient for the current business requirements.

As the project grows, bidirectional mappings may be introduced where they provide clear benefits.

---

# Current Relationships in EventFlow

```
Venue

↓

Many Halls
```

```
Hall

↓

One Venue
```

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

Future

```
Hall

↓

Many Seats
```

```
User

↓

Many Bookings
```

```
Booking

↓

Many Seats
```

---

# Why We Don't Store Objects

Instead of

```
Hall

↓

Entire Venue Object
```

the database stores

```
venueId
```

JPA reconstructs the object automatically.

This reduces duplication and keeps the database normalized.

---

# Common Beginner Mistakes

❌ Using `EAGER` everywhere.

Use `LAZY` unless eager loading is truly required.

---

❌ Forgetting `mappedBy`.

This often creates an unexpected join table.

---

❌ Adding cascade everywhere.

Cascade should only be enabled when child entities truly depend on the parent.

---

❌ Returning entities directly from controllers.

Always return DTOs instead.

---

# Best Practices Used in EventFlow

✔ Use `@ManyToOne(fetch = FetchType.LAZY)`.

✔ Keep relationships simple.

✔ Use DTOs instead of entities.

✔ Save related entities explicitly inside the Service Layer.

✔ Validate parent entities before creating children.

✔ Store foreign keys rather than duplicating data.

---

# Summary

JPA relationships allow different entities to reference each other while keeping the database normalized.

Current relationships

```
Venue

↓

Hall

↓

Event
```

Future relationships

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

Understanding these relationships is essential because every future module in EventFlow builds upon them.