# 12. Admin & Organizer Workflow

## Overview

EventFlow follows a **Role-Based Access Control (RBAC)** model.

Every authenticated user belongs to one of the predefined roles.

```
ADMIN
ORGANIZER
USER
```

Each role has a different responsibility inside the system.

---

# Why Roles?

Imagine anyone could create venues or schedule events.

```
Random User

↓

Creates fake venues

↓

Creates fake events

↓

System becomes unusable
```

Instead, permissions are separated based on responsibilities.

```
ADMIN

↓

Manages infrastructure

(Venues & Halls)

-------------------------

ORGANIZER

↓

Hosts events

-------------------------

USER

↓

Books tickets
```

This is known as **Authorization**.

Authentication answers:

> Who are you?

Authorization answers:

> What are you allowed to do?

---

# Workflow Overview

```
Register

↓

Login

↓

Receive JWT

↓

Request API

↓

JWT Validation

↓

Role Authorization

↓

Business Logic

↓

Database
```

Every protected endpoint follows this lifecycle.

---

# Admin Workflow

The administrator manages the physical infrastructure of the platform.

Current responsibilities:

- Create Venues
- Create Halls

Future responsibilities:

- Manage Seats
- Manage Organizers
- Moderate Events

---

## Admin Flow

```
Register

↓

Login

↓

JWT Generated

↓

POST /api/admin/venues

↓

Venue Created

↓

POST /api/admin/halls

↓

Hall Created
```

The administrator never creates events.

Instead, they prepare the platform for organizers.

---

# Venue Creation

Request

```
POST /api/admin/venues
```

Example

```json
{
  "name": "PVR Nexus Mall",
  "address": "Jaydev Vihar",
  "city": "Bhubaneswar",
  "state": "Odisha",
  "country": "India",
  "latitude": 20.2961,
  "longitude": 85.8245
}
```

Business Logic

```
Validate Request

↓

Authenticate Admin

↓

Create Venue

↓

Save

↓

Return VenueResponse
```

Only authenticated administrators are allowed to access this endpoint.

---

# Hall Creation

A venue may contain multiple halls.

Example

```
PVR Nexus Mall

├── Hall 1

├── Hall 2

├── Hall 3
```

Request

```
POST /api/admin/halls
```

Business Rules

- Venue must exist.
- Hall number must be unique within a venue.
- Capacity must be positive.

Flow

```
Receive Request

↓

Find Venue

↓

Venue Exists?

↓

No

↓

404 Not Found

--------------------

Yes

↓

Duplicate Hall?

↓

Yes

↓

409 Conflict

--------------------

No

↓

Create Hall

↓

Save

↓

Return HallResponse
```

---

# Organizer Workflow

Organizers are responsible for hosting events.

They do not create venues or halls.

Instead, they reserve an available hall for a specific time.

Current responsibility:

- Create Events

Future responsibilities:

- Publish Events
- Update Events
- Cancel Events

---

## Organizer Flow

```
Register

↓

Login

↓

JWT Generated

↓

POST /api/events

↓

Hall Availability Check

↓

Event Created
```

---

# Event Creation

Request

```
POST /api/events
```

Example

```json
{
  "title": "Spring Boot Workshop",
  "description": "Hands-on backend workshop",
  "bannerUrl": "https://example.com/banner.jpg",
  "hallId": "4d415308-9798-46be-b47d-f27f6c39b32a",
  "startTime": "2026-08-20T10:00:00Z",
  "endTime": "2026-08-20T13:00:00Z"
}
```

---

# Event Creation Flow

```
Receive Request

↓

Authenticate Organizer

↓

Find Organizer

↓

Find Hall

↓

Validate Time

↓

Check Hall Availability

↓

Conflict?

↓

Yes

↓

409 Conflict

-------------------

No

↓

Create Event

↓

Save

↓

Return EventResponse
```

---

# Hall Conflict Detection

A hall cannot host multiple events at the same time.

Example

```
Event A

10:00

↓

13:00
```

Trying to create

```
Event B

11:00

↓

14:00
```

Result

```
409 Conflict

Hall is already booked for this time slot.
```

This validation is performed inside the Service Layer using a custom JPQL query.

---

# Request Flow

The complete request lifecycle looks like this.

```
Client

↓

Controller

↓

Service

↓

Repository

↓

PostgreSQL

↓

Repository

↓

Service

↓

Controller

↓

Client
```

Every module inside EventFlow follows this architecture.

---

# Business Rules Implemented

## Venue

- Name is required.
- Address is required.
- Latitude is required.
- Longitude is required.

---

## Hall

- Venue must exist.
- Hall number must be unique within a venue.
- Capacity must be greater than zero.

---

## Event

- Organizer must exist.
- Hall must exist.
- Start time must be before end time.
- Hall must be available during the requested interval.

---

# HTTP Status Codes Used

| Status | Meaning |
|---------|---------|
| 201 Created | Resource successfully created |
| 400 Bad Request | Invalid input |
| 401 Unauthorized | Authentication required |
| 403 Forbidden | User does not have permission |
| 404 Not Found | Requested resource does not exist |
| 409 Conflict | Business rule violation |

---

# Current Responsibilities

| Role | Responsibilities |
|------|------------------|
| ADMIN | Create Venues, Create Halls |
| ORGANIZER | Create Events |
| USER | *(Booking functionality coming soon)* |

---

# Production Perspective

Separating responsibilities by roles makes the system:

- More secure
- Easier to maintain
- Easier to extend
- Easier to audit

This design follows the same principle used by production applications where administrators manage infrastructure while organizers manage business content.

---

# Summary

Current EventFlow workflow:

```
User Registration

↓

Login

↓

JWT Authentication

↓

Role Authorization

↓

Admin creates Venue

↓

Admin creates Hall

↓

Organizer creates Event

↓

Hall Conflict Validation

↓

Event Saved
```

At this stage, EventFlow supports a complete end-to-end workflow for administrators and organizers, providing the foundation for the upcoming ticket booking system.