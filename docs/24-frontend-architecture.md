# 24. Frontend Architecture

## Concept

The EventFlow frontend is a Single Page Application (SPA) that communicates with our Spring Boot backend exclusively via a REST API.

## Core Technologies

- **Framework:** React 18
- **Language:** TypeScript
- **Build Tool:** Vite (for rapid development and optimized production builds)
- **Routing:** React Router v6
- **Styling:** Tailwind CSS
- **UI Components:** Shadcn UI (accessible, customizable Radix UI primitives)
- **HTTP Client:** Axios (configured with interceptors)

## Architecture & Data Flow

```text
Browser                 React Router               React Context (AuthContext)
   |                          |                                 |
   | --- URL Change --------> |                                 |
   |                          | --- Check Protected Route ----> |
   |                          |                                 |
   | <--- Render Page ------- |                                 |
   |                          |
User Interaction              |
   |                          |
   | --- Form Submit -------> | --- Axios Request (apiClient)
                                            |
                                            | --- Add JWT Header
                                            ↓
                                    Spring Boot API
                                            |
                                            | <--- JSON Response
                                            ↓
                              React State (useState / useEffect)
                                            |
                                            ↓
                                     UI Update (DOM)
```

## Security & Authentication

Authentication state is managed globally using `AuthContext`.
When a user logs in, the backend returns a JWT (JSON Web Token). 

1. **Storage:** The frontend stores the JWT in `localStorage` (via the `apiClient` logic).
2. **Interceptors:** An Axios interceptor automatically attaches the `Authorization: Bearer <token>` header to all outgoing API requests.
3. **Protected Routes:** We use a `<ProtectedRoute />` component to wrap sensitive pages (like `SeatSelection`, `BookingPayment`, and `MyBookings`). If an unauthenticated user tries to access these, they are redirected to `/login`.

## Frontend Pages & Routing

Based on `App.tsx`:

- **Public Routes:**
  - `/` (Home)
  - `/login`
  - `/register`
  - `/events`
  - `/events/:eventId` (Event Details)
- **Protected Routes:**
  - `/events/:eventId/seats` (Seat Selection)
  - `/checkout` (Booking Payment with Razorpay)
  - `/success` (Booking Success)
  - `/bookings` (My Bookings)
  - `/bookings/:bookingId` (Booking Details)

## Separation of Concerns: UUIDs vs. Labels

One key architectural decision on the frontend is how it handles seats. 

The backend identifies seats strictly by `UUID` (the primary key in PostgreSQL). However, humans don't read UUIDs. They read seat labels like `A1`, `B4`.

During the `SeatSelection` flow:
1. The frontend fetches the available seats, receiving both the `UUID` and the `label` (Row + Number).
2. The UI renders the visual seat map using the labels.
3. When the user selects seats and clicks "Book", the frontend sends the list of selected `UUIDs` to the backend.

The backend does not trust the frontend's text labels; it strictly relies on the UUIDs to process database logic.
