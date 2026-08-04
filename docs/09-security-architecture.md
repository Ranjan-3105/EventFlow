# 🔐 Security Architecture

> **Module:** Authentication & Authorization  
> **Technology:** Spring Security + JWT (JSON Web Token)

---

# 📖 Overview

Security is the foundation of EventFlow.

Before a user can access protected APIs, they must first authenticate themselves. Once authenticated, the backend verifies their identity using a JWT (JSON Web Token) on every request.

The application follows a **stateless authentication** approach, meaning the server does **not** store login sessions. Every request carries its own authentication information.

---

# Why JWT?

Imagine a user logs into EventFlow.

Without JWT, the server would have to remember:

- Who the user is
- Whether they are logged in
- When their session expires

This requires storing sessions on the server.

Instead, JWT stores the authentication information inside a digitally signed token.

Example:

```
Login
    ↓
Server
    ↓
Generate JWT
    ↓
Client stores JWT
    ↓
Every future request includes JWT
```

The server simply validates the token instead of remembering every logged-in user.

---

# Stateless Authentication

EventFlow uses:

```java
SessionCreationPolicy.STATELESS
```

This tells Spring Security:

> "Never create or store HTTP Sessions."

Every request must contain a valid JWT.

Advantages:

- Better scalability
- No server-side session storage
- Suitable for REST APIs
- Easier deployment across multiple servers

---

# Authentication vs Authorization

These two concepts are often confused.

## Authentication

Authentication answers:

> **Who are you?**

Example:

```
Email
Password
```

If correct:

```
User authenticated.
```

---

## Authorization

Authorization answers:

> **What are you allowed to do?**

Example:

```
USER
```

Cannot create venues.

```
ADMIN
```

Can create venues.

---

# Security Components

Our security module consists of the following components.

```
SecurityConfig

↓

JwtAuthenticationFilter

↓

JwtService

↓

CustomUserDetailsService

↓

JwtAuthenticationEntryPoint

↓

CustomAccessDeniedHandler
```

Each component has a specific responsibility.

---

# 1. SecurityConfig

This is the central configuration class for Spring Security.

Responsibilities:

- Disable CSRF
- Configure public endpoints
- Configure protected endpoints
- Configure role-based authorization
- Register JWT filter
- Configure stateless sessions
- Configure exception handling

Example:

```java
http
    .csrf(csrf -> csrf.disable())

    .authorizeHttpRequests(...)

    .sessionManagement(...)

    .exceptionHandling(...)

    .addFilterBefore(...)
```

Think of this class as the **security rulebook** of the application.

---

# 2. JWT Filter

Class:

```
JwtAuthenticationFilter
```

This filter executes before every protected request.

Responsibilities:

- Read Authorization header
- Extract JWT
- Validate token
- Load user
- Store authenticated user inside Spring Security

Flow:

```
Incoming Request

↓

Authorization Header

↓

Extract JWT

↓

Validate JWT

↓

Load User

↓

SecurityContextHolder

↓

Continue Request
```

If the token is invalid:

```
401 Unauthorized
```

is returned immediately.

---

# 3. JwtService

This class performs every JWT-related operation.

Responsibilities:

- Generate Token
- Extract Username
- Validate Token
- Check Expiration
- Parse Claims

Example methods:

```
generateToken()

extractUsername()

extractClaim()

isTokenValid()
```

Instead of spreading JWT logic throughout the project, everything stays inside this service.

---

# 4. CustomUserDetailsService

Spring Security needs a way to load users.

Instead of writing SQL manually every time,

Spring calls:

```
loadUserByUsername(email)
```

which internally performs:

```
Database

↓

UserRepository

↓

findByEmail()

↓

User
```

The returned User implements:

```
UserDetails
```

which Spring Security understands.

---

# 5. AuthenticationManager

Used only during login.

Flow:

```
Login Request

↓

AuthenticationManager

↓

UserDetailsService

↓

PasswordEncoder

↓

Success / Failure
```

If authentication succeeds:

```
JWT Generated
```

Otherwise:

```
401 Unauthorized
```

---

# 6. SecurityContextHolder

This is one of the most important Spring Security classes.

Once authentication succeeds,

Spring stores the authenticated user inside:

```
SecurityContextHolder
```

Later we can retrieve the currently logged-in user:

```java
Authentication authentication =
    SecurityContextHolder
        .getContext()
        .getAuthentication();
```

From there:

```java
String email = authentication.getName();
```

This is how EventService identifies the organizer without the client sending an organizer ID.

---

# 7. JwtAuthenticationEntryPoint

Handles authentication failures.

Example:

- Missing JWT
- Expired JWT
- Invalid JWT

Returns:

```json
{
  "status":401,
  "message":"Authentication required"
}
```

---

# 8. CustomAccessDeniedHandler

Authentication succeeded.

But the user lacks permission.

Example:

```
USER

↓

POST /api/admin/venues
```

Result:

```
403 Forbidden
```

Returned JSON:

```json
{
  "status":403,
  "message":"Access denied"
}
```

---

# Complete Login Flow

```
Client

↓

POST /api/auth/login

↓

AuthController

↓

AuthService

↓

AuthenticationManager

↓

CustomUserDetailsService

↓

PasswordEncoder

↓

Credentials Valid?

      ↓

     YES

↓

JwtService

↓

Generate JWT

↓

Return Token
```

---

# Protected Request Flow

```
Client

↓

Authorization:
Bearer <JWT>

↓

JwtAuthenticationFilter

↓

Extract JWT

↓

JwtService

↓

Validate JWT

↓

Load User

↓

SecurityContextHolder

↓

Controller

↓

Service

↓

Repository

↓

Database
```

---

# Role-Based Authorization

EventFlow currently defines three roles.

```
ADMIN

ORGANIZER

USER
```

Current permissions:

| Role | Permissions |
|-------|-------------|
| ADMIN | Manage Venues, Halls, Seats |
| ORGANIZER | Create & Manage Events |
| USER | Book Events |

---

# Exception Handling

Authentication errors:

```
401 Unauthorized
```

Examples:

- Missing Token
- Invalid Token
- Expired Token

Authorization errors:

```
403 Forbidden
```

Examples:

- USER accessing Admin APIs
- Organizer accessing Admin APIs

---

# Why We Used JWT

Advantages:

- Stateless
- Scalable
- Fast
- Easy to integrate with REST APIs
- Works well with mobile applications
- No server-side session storage

---

# Current Security Features

Implemented:

- ✅ User Registration
- ✅ User Login
- ✅ BCrypt Password Encryption
- ✅ JWT Generation
- ✅ JWT Validation
- ✅ Stateless Authentication
- ✅ Role-Based Authorization
- ✅ Custom AuthenticationEntryPoint
- ✅ Custom AccessDeniedHandler
- ✅ Global Exception Handling
- ✅ Validation
- ✅ SecurityContext-based User Identification

---

# Future Improvements

Potential enhancements:

- Refresh Tokens
- Email Verification
- Password Reset
- Account Locking
- Login Rate Limiting
- OAuth2 (Google Login)
- Multi-Factor Authentication (MFA)
- Audit Logging

---

# Summary

The security architecture of EventFlow is designed around Spring Security and JWT authentication.

Instead of maintaining server-side sessions, every request is authenticated using a JWT. Spring Security validates the token, loads the authenticated user, stores it in the SecurityContext, and enforces role-based authorization before allowing access to protected resources.

This architecture provides a secure, scalable, and maintainable foundation for the entire application.