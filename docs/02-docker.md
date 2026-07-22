# Docker Setup

## Why Docker?

Instead of installing PostgreSQL directly on my operating system, I run it inside a Docker container.

Advantages:

- No manual installation.
- Same environment across every machine.
- Easy to start and stop.
- Easy to delete and recreate.
- Required later for microservices deployment.

---

## Docker Concepts

### Image

An image is a blueprint used to create a container.

Example:

```yaml
image: postgres:17
```

This tells Docker to use PostgreSQL version 17.

Think of it as a Java class.

---

### Container

A running instance of an image.

Think of it as a Java object created from a class.

```
Image
    ↓
Container
```

---

### Port Mapping

```yaml
ports:
  - "5432:5432"
```

Format:

```
HOST_PORT : CONTAINER_PORT
```

Since Spring Boot currently runs on my local machine, it accesses PostgreSQL through:

```
localhost:5432
```

Later, when Spring Boot is containerized, it will communicate using Docker networking instead of localhost.

---

### Volume

```yaml
volumes:
  - postgres-data:/var/lib/postgresql/data
```

A volume stores PostgreSQL data outside the container.

Without a volume:

```
Delete Container
↓

Database Deleted
```

With a volume:

```
Delete Container
↓

Volume Still Exists
↓

New Container
↓

Data Restored
```

---

### Environment Variables

```yaml
POSTGRES_USER
POSTGRES_PASSWORD
POSTGRES_DB
```

These initialize PostgreSQL on the first startup.

Docker automatically creates:

- Database
- User
- Password

No manual SQL is required.

---

### Health Check

```yaml
pg_isready
```

Docker periodically checks if PostgreSQL is ready to accept connections.

This becomes useful when other services depend on the database.