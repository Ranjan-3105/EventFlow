# 19. Seat Module

## Overview

The Seat module is responsible for creating the seating layout of a hall.

Unlike other entities, seats are **not created manually one by one**. Instead, they are generated automatically from a simple layout configuration.

Example:

```
Rows: 10
Seats Per Row: 18
```

generates

```
A1 A2 A3 ... A18

B1 B2 B3 ... B18

...

J1 J2 J3 ... J18
```

This approach keeps the API simple while ensuring every seat follows a predictable naming convention.

---

# Why a Separate Seat Entity?

A hall represents only the physical room.

It does **not** contain information about individual seats.

```
Hall

↓

Many Seats
```

Each seat has its own identity because later it will be:

- Booked
- Locked
- Reserved
- Cancelled
- Refunded

Therefore seats must exist as independent entities.

---

# Entity Design

```
Seat
----------------------------

id

rowLabel

seatNumber

seatType

hall

createdAt

updatedAt
```

Relationship:

```
Many Seats

↓

One Hall
```

Each seat belongs to exactly one hall.

---

# Seat Identification

Instead of storing a single string like

```
A12
```

the system stores

```
rowLabel = A

seatNumber = 12
```

Advantages:

- Easier searching
- Better sorting
- Simpler generation
- Cleaner database design

The frontend can always display

```
A12
```

by combining both values.

---

# Seat Types

Currently every generated seat uses

```
EXECUTIVE
```

Future versions may support:

```
EXECUTIVE

VIP

PREMIUM

RECLINER

WHEELCHAIR
```

Using an enum makes extending seat categories straightforward.

---

# Automatic Seat Generation

Instead of manually inserting hundreds of seats, the administrator only provides:

```
Rows

Seats Per Row
```

The backend generates every seat automatically.

Example:

```
Rows = 3

Seats = 4
```

Generated layout:

```
A1 A2 A3 A4

B1 B2 B3 B4

C1 C2 C3 C4
```

The algorithm uses two nested loops.

Outer loop

```
Rows
```

Inner loop

```
Seats inside each row
```

---

# Hall Capacity

The administrator never enters hall capacity.

Capacity is calculated automatically.

```
Generated Seats

↓

Count

↓

Hall Capacity
```

Example:

```
10 Rows

18 Seats Per Row

↓

180 Seats

↓

Capacity = 180
```

This guarantees that hall capacity always matches the actual number of seats.

---

# Business Rules

## Hall must exist

Seat generation is allowed only for existing halls.

Otherwise

```
404 Not Found
```

is returned.

---

## Generate only once

A seat layout cannot be generated multiple times.

If seats already exist:

```
409 Conflict

Seat layout already exists.
```

This prevents accidental duplicate layouts.

Future layout modifications will be implemented as a dedicated update workflow instead of regenerating seats.

---

## Maximum Rows

Current limit:

```
26
```

Reason:

Rows are represented using letters.

```
A

B

...

Z
```

Support for layouts beyond Z (AA, AB, etc.) can be added later if required.

---

## Maximum Seats Per Row

Current limit:

```
50
```

This prevents unrealistic layouts and accidental generation of extremely large datasets.

---

# Performance

Seats are generated in memory first.

```
Generate

↓

ArrayList

↓

saveAll()
```

Using

```
saveAll()
```

is significantly more efficient than saving each seat individually.

---

# Database Constraints

Every seat must be unique within a hall.

Unique Constraint:

```
hall_id

row_label

seat_number
```

Examples:

Allowed

```
Hall 1

A1
```

```
Hall 2

A1
```

Not Allowed

```
Hall 1

A1

Hall 1

A1
```

---

# Validation

DTO Validation

- Rows must be greater than 0
- Seats per row must be greater than 0

Business Validation

- Maximum 26 rows
- Maximum 50 seats per row
- Hall must exist
- Seat layout can only be generated once

---

# API

Generate Seats

```
POST

/api/admin/halls/{hallId}/generate-seats
```

Request

```json
{
  "rows": 10,
  "seatsPerRow": 18
}
```

Response

```json
{
  "hallId": "...",
  "totalSeats": 180,
  "message": "Seats generated successfully."
}
```

---

# Future Improvements

- JSON layout import
- CSV layout import
- Multiple seat categories
- Seat pricing
- Disabled seats
- Dynamic layout editor
- Support for rows beyond Z (AA, AB, AC...)

---

# Summary

The Seat module converts a simple hall configuration into a complete seating layout.

Key features include:

- Automatic seat generation
- Automatic hall capacity calculation
- Validation of realistic layouts
- Duplicate generation prevention
- Efficient batch persistence
- Extensible seat type design

This module forms the foundation for future booking, seat locking, payment, and ticket generation features.