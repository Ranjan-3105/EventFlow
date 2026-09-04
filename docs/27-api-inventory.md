# 27. Final API Inventory

This document outlines the final REST API endpoints exposed by the EventFlow Spring Boot backend.

## Authentication (`/api/auth`)

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |

## Admin: Venues (`/api/admin/venues`)

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/admin/venues` | `ADMIN` only | Create a new venue |

## Admin: Halls (`/api/admin/halls`)

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/admin/halls` | `ADMIN` only | Create a new hall inside a venue |
| POST | `/api/admin/halls/{hallId}/generate-seats` | `ADMIN` only | Auto-generate the physical seat layout for a hall |

## Events (`/api/events`)

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/events` | Authenticated | Create a new event |
| GET | `/api/events` | Authenticated | Fetch all published events (Cached) |
| GET | `/api/events/{eventId}` | Authenticated | Fetch details of a specific event |
| GET | `/api/events/{eventId}/seats` | Authenticated | Fetch real-time seat availability for an event |
| POST | `/api/events/{eventId}/publish` | Authenticated | Publish an event (Invalidates cache) |

## Bookings (`/api/bookings`)

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/bookings` | Authenticated | Create a new PENDING booking and lock seats |
| GET | `/api/bookings` | Authenticated | Get all bookings for the currently authenticated user |
| GET | `/api/bookings/{bookingId}` | Authenticated | Get details of a specific booking |
| POST | `/api/bookings/{bookingId}/cancel` | Authenticated | Cancel a booking |

## Payments (`/api/payments`)

| Method | Path | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/payments` | Authenticated | Create a Razorpay Order for a PENDING booking |
| POST | `/api/payments/verify` | Authenticated | Verify Razorpay signature and confirm booking |
