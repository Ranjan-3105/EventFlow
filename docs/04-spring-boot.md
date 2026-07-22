# Spring Boot Startup Flow

When the application starts, the following sequence occurs:

```
JVM
    ↓
SpringApplication.run()
    ↓
Reads application.properties
    ↓
Creates ApplicationContext
    ↓
Creates DataSource (HikariCP)
    ↓
Connects to PostgreSQL
    ↓
Initializes Hibernate
    ↓
Scans @Entity classes
    ↓
Creates EntityManagerFactory
    ↓
Starts Embedded Tomcat
    ↓
Application Ready
```

## Components initialized

- Embedded Tomcat
- HikariCP Connection Pool
- Hibernate ORM
- Spring Security
- Spring Data JPA

## Current Status

✔ Database Connected

✔ Tomcat Running

✔ Security Enabled

✔ Hibernate Initialized

❌ No Entities Yet

❌ No REST APIs Yet