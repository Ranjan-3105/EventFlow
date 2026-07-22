# PostgreSQL Notes

## Databases present after installation

```
eventdb
postgres
template0
template1
```

---

## eventdb

This is the application's database.

All application tables will be created here by Hibernate.

Examples:

- users
- events
- bookings
- payments

---

## postgres

Default administrative database.

Used mainly for maintenance and server administration.

The application does not use this database.

---

## template0

A clean template database.

Whenever PostgreSQL creates a new database, it copies from a template instead of creating everything from scratch.

template0 remains untouched.

---

## template1

The default template used when creating new databases.

Custom extensions can be installed here so that every future database automatically contains them.

---

## Connecting using psql

```
docker exec -it eventflow-postgres psql -U admin -d eventdb
```

Useful commands:

```
\l      List databases

\dt     List tables

\d      Describe a table

\q      Quit
```