# Docker Setup Guide

This document explains how to run the PostgreSQL database used by EventFlow.

---

## Prerequisites

- Docker Desktop installed
- Docker Compose enabled

Verify installation:

```bash
docker --version
docker compose version
```

---

## Start the Database

From the project root:

```bash
docker compose up -d
```

Verify the container is running:

```bash
docker ps
```

Expected:

```
eventflow-postgres
```

---

## Stop the Database

```bash
docker compose down
```

---

## View Logs

```bash
docker logs eventflow-postgres
```

Follow logs continuously:

```bash
docker logs -f eventflow-postgres
```

---

## Restart Database

```bash
docker compose restart
```

---

## Connect using DBeaver

Host:

```
localhost
```

Port:

```
5432
```

Database:

```
eventdb
```

Username:

```
admin
```

Password:

```
password
```

---

## Connect using psql

```bash
docker exec -it eventflow-postgres psql -U admin -d eventdb
```

Useful commands:

List databases

```sql
\l
```

Switch database

```sql
\c eventdb
```

List tables

```sql
\dt
```

Describe table

```sql
\d users
```

View data

```sql
SELECT * FROM users;
```

Exit

```sql
\q
```

---

## Common Docker Commands

List containers

```bash
docker ps
```

Stop container

```bash
docker stop eventflow-postgres
```

Start container

```bash
docker start eventflow-postgres
```

Remove container

```bash
docker rm eventflow-postgres
```

Remove all containers

```bash
docker container prune
```

---

## Troubleshooting

### Port 5432 already in use

Check which process is using the port or change the mapped port in
`docker-compose.yml`.

---

### Database does not exist

Verify the database name matches the one in
`application.properties`.

Example:

```
eventdb
```

---

### Authentication failed

Verify:

- username
- password
- database

match the values in `docker-compose.yml`.

---

## Current Configuration

Container

```
eventflow-postgres
```

Database

```
eventdb
```

Username

```
admin
```

Port

```
5432
```