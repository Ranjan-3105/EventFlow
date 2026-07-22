# Product Requirements Document
## EventFlow — Distributed Event Ticket Booking Platform

---

## 1. Vision & Positioning

EventFlow is a production-grade event ticket booking platform (BookMyShow/Eventbrite-class) built to demonstrate real backend engineering depth — distributed systems, concurrency safety, scalability, and security — not just CRUD.

**What makes it stand out from typical portfolio clones:**
- Correct seat-locking under concurrent load (no double-booking, proven with load tests)
- Real event-driven architecture (Kafka), not just synchronous REST calls
- Defense-in-depth security (JWT + refresh rotation, RBAC, rate limiting, audit logs)
- Observability from day one (metrics, tracing, structured logs)
- Cloud-native deployment (Docker → Kubernetes → AWS) with autoscaling
- Advanced commerce features (dynamic pricing, waitlists, fraud rules, ticket transfer) layered on top of a stable core — these come *last*, not first

**Guiding build principle:** get one thing working end-to-end (a user can register, browse, and book one seat) before adding a second service. Depth before breadth. Every phase below ends with a runnable, demoable increment.

---

## 2. Non-Functional Requirements (apply across all phases)

| Category | Requirement |
|---|---|
| Consistency | No two confirmed bookings may ever hold the same seat for the same event schedule |
| Availability | Seat availability reads should be fast (<200ms p95) even under cache-cold conditions |
| Security | All state-changing endpoints require auth + validated input; secrets never in source control |
| Idempotency | Payment and booking creation endpoints must be safe to retry |
| Observability | Every service emits structured logs, metrics, and traces from its first commit |
| Testability | Every service ships with unit tests; booking/seat flow ships with integration + load tests |

---

## 3. Technology Stack (locked in early, used throughout)

- **Backend:** Java 21, Spring Boot, Spring Security, Spring Data JPA, Spring Cloud
- **Frontend:** Next.js, React, Tailwind CSS, React Query, Zustand
- **Database:** PostgreSQL
- **Cache:** Redis
- **Message Broker:** Apache Kafka
- **Search:** Elasticsearch (introduced later, not day one)
- **Storage:** AWS S3
- **Auth:** JWT + OAuth2
- **Observability:** Prometheus, Grafana, OpenTelemetry, SLF4J/Logback
- **Containerization/Orchestration:** Docker, Docker Compose → Kubernetes
- **CI/CD:** GitHub Actions
- **Cloud:** AWS (EC2/EKS, RDS, ElastiCache, S3, CloudFront, ECR, SES, CloudWatch, ALB)

---

## 4. High-Level Architecture (target end-state)

```
Client (Next.js)
      │
  API Gateway
      │
 ┌────┼─────────────────────────────────────────┐
 │    │        │        │        │        │      │
User  Event   Seat    Booking  Payment  Search  Recommendation
Svc   Svc     Svc     Svc      Svc      Svc     Svc
 │    │        │        │        │        │      │
 └──────────────  Kafka (async events)  ──────────┘
                       │
              Notification / Analytics Consumers
```

This is the **destination**, not the starting point. You build a monolith-friendly core first and split services only once each concern is stable — this avoids premature distributed-systems complexity.

---

## 5. Phased Roadmap

Each phase lists: goal, scope, database tables introduced, and exit criteria (how you know it's done). Security and observability tasks are embedded per-phase rather than deferred to "later."

### Phase 0 — Foundation & Project Skeleton
**Goal:** A running, empty, deployable skeleton before any feature code.
- Monorepo/service structure decision (start as a modular monolith; extract microservices later)
- Docker Compose with Postgres + Redis stubs
- Base Spring Boot app with Actuator health checks
- GitHub Actions pipeline: run tests → build → Docker image (deploy step stubbed for now)
- SLF4J/Logback structured logging baseline
- `.env`/secret management convention (no secrets in git)

**Tables:** none yet (schema starts Phase 1)
**Exit criteria:** `docker-compose up` boots the app; `/actuator/health` returns 200; CI pipeline is green on push.

---

### Phase 1 — User Service: Auth & RBAC
**Goal:** Users can register, log in, and hold a role.
- Email/password registration + login
- Password hashing (BCrypt)
- JWT access + refresh tokens, refresh token rotation/revocation
- Role-Based Access Control: Customer, Organizer, Admin
- Google OAuth2 login
- Input validation, basic rate limiting on auth endpoints
- Audit log entries for login/failed-login

**Tables:** `Users`, `Roles`, `Permissions`, `RefreshTokens`, `AuditLogs`
**Exit criteria:** A user can register, log in, receive JWT + refresh token, refresh an expired token, and hit a role-protected endpoint (e.g., Organizer-only) correctly rejecting Customers.

---

### Phase 2 — Event & Venue Management (Organizer core)
**Goal:** Organizers can create and manage events; anyone can browse them.
- Venue CRUD
- Event CRUD (title, description, images, category)
- Event schedules (multiple dates per event)
- Ticket types per schedule (VIP/Regular/Premium) with price + capacity
- Categories
- Secure file upload for event images → S3
- Basic pagination on public "list events" endpoint

**Tables:** `Organizers`, `Venues`, `Events`, `EventSchedules`, `TicketTypes`
**Exit criteria:** An Organizer account can create an event with a venue, a schedule, and multiple ticket types; a public API lists/paginates events by category and city.

---

### Phase 3 — Basic Search & Discovery (DB-backed)
**Goal:** Users can find events without needing Elasticsearch yet.
- Search by name, city, category, date range, price range (Postgres queries + indexes)
- Filter combinations
- "Trending" as a simple query (bookings count in last N days) — real recommendation engine comes later

**Tables:** indexes added to `Events`/`EventSchedules`; no new tables
**Exit criteria:** Filtered/paginated search returns correct results under realistic seed data (1000+ events).

---

### Phase 4 — Seat Management & Locking (the hard part, built early on purpose)
**Goal:** Seat-level inventory with correct concurrency handling — this is the platform's core differentiator, so it's proven before booking/payment are layered on.
- Seat map generation per venue/schedule, seat categories
- Live seat availability endpoint
- Seat locking strategy — pick and implement one, document the tradeoff:
  - Redis distributed lock with TTL (seat reservation timeout), or
  - `SELECT FOR UPDATE` + DB row versioning (pessimistic), or
  - Optimistic locking with version column + retry
- Seat release on timeout (scheduled job or Redis key expiry)
- Idempotency keys on "lock seat" requests

**Tables:** `Seats`
**Exit criteria:** A k6/JMeter load test hammers the same seat with concurrent requests — exactly one lock succeeds, others get a clean 409, and locks expire correctly if unconfirmed. **Do not proceed to Phase 5 until this passes under load.**

---

### Phase 5 — Booking Service
**Goal:** A locked seat becomes a real booking.
- Create booking (converts a seat lock into a pending booking)
- Booking cancellation
- Booking history for a user
- Booking status state machine (PENDING → CONFIRMED → CANCELLED/EXPIRED)
- Idempotency on booking creation

**Tables:** `Bookings`, `BookingItems`
**Exit criteria:** End-to-end flow works: browse → lock seat → create booking → booking appears in history with correct status transitions.

---

### Phase 6 — Payment Integration
**Goal:** Bookings become paid, real transactions.
- Razorpay/Stripe integration (pick one first, abstract via Strategy pattern so the other can be added later)
- Payment verification
- Payment webhooks (signature verification!)
- Refunds
- Invoice generation (PDF)
- Booking status updates to CONFIRMED only on verified payment success

**Tables:** `Payments`, `Refunds`
**Exit criteria:** A test payment (sandbox mode) confirms a booking; a webhook failure/timeout does not leave the seat double-booked or double-charged; a refund correctly releases funds and updates booking status.

---

### Phase 7 — Event-Driven Architecture (Kafka) & Notifications
**Goal:** Decouple side effects from the booking/payment critical path.
- Introduce Kafka; publish `BookingCreated` and `PaymentSuccess`/`PaymentFailure` events
- Notification Service as first consumer: booking confirmation, payment success/failure, cancellation emails
- QR code generation for confirmed tickets, downloadable ticket

**Tables:** `Notifications`
**Exit criteria:** Booking/payment flows publish events; Notification Service consumes independently and can be restarted/fail without blocking booking creation.

---

### Phase 8 — Caching Layer (Redis)
**Goal:** Now that the domain model is stable, cache the hot paths.
- Cache: popular events, event details, seat availability snapshots, categories, cities, trending events, user sessions
- Cache invalidation strategy tied to booking/event-update events (via Kafka consumers)

**Exit criteria:** Cache-hit p95 latency measurably improves over Phase 3 baseline; stale-seat-availability bugs are covered by a specific test (booking a seat correctly invalidates the cached availability).

---

### Phase 9 — Elasticsearch Search Upgrade
**Goal:** Replace/augment DB search with proper full-text + faceted search.
- Index events into Elasticsearch (synced via Kafka `EventCreated`/`EventUpdated` events)
- Search by name, venue, city, artist, category, date, price, popularity
- Reviews and Wishlist can be added here as supporting features tied to discovery

**Tables:** `Reviews`, `Wishlist`
**Exit criteria:** Search latency and relevance improve over Phase 3; index stays in sync with Postgres via events, not dual writes from the API layer.

---

### Phase 10 — Recommendation Service
**Goal:** Personalized and trending recommendations, now backed by real event/booking history.
- Trending (derived from booking event stream)
- Personalized recommendations (simple collaborative filtering or rules-based to start)

**Exit criteria:** Recommendation endpoint returns different results for different user booking histories.

---

### Phase 11 — Organizer Dashboard
**Goal:** Organizers get visibility into their own events.
- Revenue dashboard, ticket sales, attendance, customer analytics
- Backed by Analytics Service, itself a Kafka consumer of booking/payment events (not synchronous queries against the booking DB)

**Exit criteria:** Dashboard numbers reconcile against raw booking/payment tables for a test dataset.

---

### Phase 12 — Admin Dashboard
**Goal:** Platform-wide control and oversight.
- Manage users, organizers, events, payments
- Reports
- Audit log viewer (using logs already captured since Phase 1)
- Coupons management (introduces `Coupons` table, used again in Phase 13)

**Tables:** `Coupons`
**Exit criteria:** Admin can suspend a user/organizer, void an event, and view an audit trail of a booking's lifecycle.

---

### Phase 13 — Advanced Commerce Features
**Goal:** Differentiating features layered on a now-stable core. Build in this order since each depends on the last:
1. Promo codes / Coupons (uses table from Phase 12)
2. Dynamic pricing (price adjusts based on demand/time-to-event)
3. Loyalty points
4. Referral system
5. Waitlist system (for sold-out schedules)
6. Ticket transfer (between users)
7. Event check-in system + QR code validation (uses QR from Phase 7)
8. Fraud detection rules (bulk-booking patterns, card testing, etc.)
9. Multi-currency support
10. Multi-language support
11. Feature flags (so remaining rollouts can be toggled safely)
12. Scheduled jobs / event reminder scheduler
13. Soft deletes, versioned APIs (retrofit as a cross-cutting cleanup pass)

**Exit criteria:** Each feature ships behind a feature flag and has its own test coverage; none of these should require changes to the seat-locking or payment-verification core logic.

---

### Phase 14 — Full Observability
**Goal:** Production-grade visibility (basic logging existed since Phase 0; this phase completes the stack).
- Prometheus metrics across all services
- Grafana dashboards (booking success rate, payment failure rate, seat-lock contention)
- OpenTelemetry distributed tracing across the now-split services
- Alerting on key SLOs (payment webhook failures, seat-lock timeout spikes)

**Exit criteria:** A single booking request can be traced end-to-end across services in Grafana/Jaeger-style trace view.

---

### Phase 15 — Containerization & Orchestration at Scale
**Goal:** Move from Docker Compose to Kubernetes.
- Multi-stage Docker builds per service
- Kubernetes manifests: Pods, Services, Ingress, ConfigMaps, Secrets
- Horizontal Pod Autoscaler tuned against real load-test metrics from Phase 4/6

**Exit criteria:** The full platform runs on a local (kind/minikube) or managed Kubernetes cluster; autoscaling triggers correctly under a simulated traffic spike.

---

### Phase 16 — Cloud Deployment (AWS)
**Goal:** Production deployment.
- RDS PostgreSQL, ElastiCache Redis, S3 + CloudFront for images, ECR for images, EKS for orchestration, SES for email, ALB, CloudWatch
- GitHub Actions CI/CD extended to deploy to AWS

**Exit criteria:** A production-like environment is reachable over HTTPS, deploys automatically on merge to main, and survives a basic chaos test (kill a pod, confirm self-healing).

---

### Phase 17 — Testing Hardening & Documentation
**Goal:** Close the loop — this is ongoing but formalized at the end.
- Contract testing between services
- End-to-end test suite across the full booking journey
- Load testing (k6/JMeter) repeated against the full distributed system (not just the seat service in isolation)
- Final documentation pass: README, OpenAPI/Swagger docs, architecture diagram, ER diagram, sequence diagrams (seat-lock flow, payment flow), deployment guide, Postman collection

**Exit criteria:** A new engineer can clone the repo, follow the README, and get a working local environment without asking you a question.

---

## 6. Design Patterns Applied (woven in, not bolted on)

| Pattern | Where it's used |
|---|---|
| Repository Pattern | All Spring Data JPA data access (Phase 1+) |
| Service Layer | Every service module (Phase 1+) |
| Strategy Pattern | Payment provider abstraction (Phase 6), seat-locking strategy (Phase 4) |
| Factory Pattern | Notification channel creation — email/SMS/push (Phase 7) |
| Builder Pattern | Complex object construction — event/schedule/ticket-type creation (Phase 2) |
| Observer Pattern | Kafka consumers reacting to booking/payment events (Phase 7+) |
| Dependency Injection | Throughout, via Spring |
| CQRS (optional) | Consider only for Analytics Service (Phase 11) if read/write load diverges significantly |

---

## 7. Explicit Ordering Rationale (why this order, not another)

1. **Auth before everything** — every other service needs to know who's calling.
2. **Events/Venues before Search** — you need real data before indexing it.
3. **Seat locking before Booking, before Payment** — this is the hardest correctness problem in the whole system; proving it under load early means every later phase builds on a foundation you already trust, instead of discovering a race condition after payments are wired in.
4. **Kafka/Notifications after Booking+Payment exist** — there's nothing meaningful to publish before these events happen.
5. **Caching after the domain model stabilizes** — caching a schema that's still changing wastes effort and hides bugs.
6. **Elasticsearch after basic DB search** — you get a working search experience fast, then upgrade it, rather than blocking on ES setup before you have anything to search.
7. **Dashboards after Analytics events exist** — Organizer/Admin dashboards should read from event streams, not live production tables, so they must wait for Kafka.
8. **Advanced commerce features last** — they're differentiators, not dependencies. Nothing in Phases 0–12 needs promo codes or loyalty points to function.
9. **Full observability/K8s/AWS near the end** — you want these wrapping a system whose service boundaries have already stabilized, not chasing infra changes while the domain model is still moving.

---

## 8. Suggested Definition of "MVP" (if you need a demo before Phase 17)

Phases 0–7 constitute a genuinely demoable MVP: auth, event creation, seat locking under concurrency, booking, real payment, and async notifications. This alone is enough to showcase the "not just CRUD" distributed-systems story — everything after Phase 7 is depth and polish.