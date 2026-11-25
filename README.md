# Fasor Hub

Book campus sports facilities without chasing anyone for a schedule.

![Java 21](https://img.shields.io/badge/Java-21-orange)
![Tomcat 10.1](https://img.shields.io/badge/Tomcat-10.1-yellow)
![MySQL 8](https://img.shields.io/badge/MySQL-8-blue)

## Overview

Fasor Hub is a web application for requesting and managing court sessions at a campus sports complex. Borrowers pick a facility and a date, request up to four sessions, and follow the decision. Facility managers verify each request, keep the schedule accurate, and handle cancellations.

The system treats a session as a shared resource: two requests can never end up holding the same session, even when managers act at the same moment.

> [!NOTE]
> Fasor Hub is still being built. Borrowing, verification, cancellation, and schedule and facility management work end to end. Booking history and usage reports are not done yet. See the [project board](https://github.com/users/musyaree/projects/4) for what is next.

## Try it

Live demo: **https://fasor-hub.up.railway.app**

The demo runs on a free plan, so the first request after a quiet period can take a few seconds. Sign in with one of the demo accounts. All of them use the password `password`.

| Email | Role |
|---|---|
| `alice@example.com` | Borrower |
| `manager@example.com` | Manager |

The demo holds sample data only. Please do not enter real personal details.

### As a borrower

Sign in as `alice@example.com`.

1. Open **Schedule & Booking**, pick a facility, and choose a date on the calendar.
2. Select one to four sessions, fill in the applicant details, and submit. Accept the terms when asked.
3. Open **Booking Status** and select the booking to see its details and decision.
4. Open **Cancellation**, select a pending or approved booking, give a reason, and request the cancellation.

### As a manager

Sign in as `manager@example.com`.

1. Open **Verification**, select the request you just submitted, then approve it or reject it with a reason.
2. Open **Manage Schedule** to open or close sessions for a date.
3. Open **Manage Facilities** to add, edit, or delete a facility, and to manage facility types.
4. Open **Cancellation Requests** to confirm a cancellation that a borrower asked for.

Switch back to the borrower account to see the decision in **Booking Status**. A request holds no session until it is approved, and an approved booking releases its sessions when its cancellation is confirmed.

## Features

| Feature | Status |
|---|---|
| Database schema, seed data, and message catalog | Available |
| Register, log in, and role-based access | Available |
| Facility calendar and session availability | Available |
| Request one to four sessions | Available |
| Booking status | Available |
| Approve a booking and occupy all of its sessions, or none | Available |
| Reject a booking with a reason | Available |
| Cancellation requests and confirmation | Available |
| Open and close sessions | Available |
| Facility and facility type management | Available |
| Automatic handling of overdue bookings | Available |
| Session generation for new days on existing facilities | Planned |
| Booking history | Planned |
| Usage reports and list filters | Planned |

## How a booking works

<p align="center">
  <img src="docs/images/booking-flow.png" alt="Booking lifecycle: a submitted booking is Pending Verification, then Approved, Rejected, or Pending Cancellation; Pending Cancellation ends as Cancelled." width="560">
</p>

- A request does not hold any session until a manager approves it.
- Approval occupies every session of the booking together. If one session is no longer available, nothing changes.
- Cancelling an approved booking releases its sessions. Cancelling a pending one has no effect on the schedule.
- Rejected and cancelled bookings are final. A borrower who still wants the court submits a new request.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Web | Jakarta Servlet 6, JSP 3.1, JSTL 3 on Tomcat 10.1 |
| Data access | JDBC with HikariCP |
| Database | MySQL 8 |
| Security | BCrypt password hashing |
| Build and test | Maven, JUnit 5 |

## Getting started

### Prerequisites

- JDK 21 and Maven 3.9 or later
- Docker, or a MySQL 8 server you can reach

### Start MySQL

```bash
docker run -d --name fasor-mysql -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=fasor -e MYSQL_DATABASE=fasor_hub \
  -e MYSQL_USER=fasor -e MYSQL_PASSWORD=fasor \
  mysql:8.0 --default-time-zone=+07:00
```

### Build and run

```bash
mvn package
docker build -t fasor-hub .
docker run --rm --network host -e SEED_ON_START=true fasor-hub
```

The schema is created on startup. With `SEED_ON_START=true`, an empty database is filled with demo data: four accounts, four facilities, 35 days of sessions, and 13 sample bookings.

Open `http://localhost:8080` once the container is running.

### Demo accounts

All demo accounts use the password `password`.

| Email | Role |
|---|---|
| `alice@example.com` | Borrower |
| `bob@example.com` | Borrower |
| `carol@example.com` | Borrower |
| `manager@example.com` | Manager |

> [!WARNING]
> Demo data uses a known password. The public demo is seeded on purpose and holds sample data only. For any deployment with real users, leave `SEED_ON_START` unset.

### Run the tests

The tests use a real MySQL database, not SQLite, because conflict tests need row locking. Create it once:

```bash
docker exec fasor-mysql mysql -uroot -pfasor -e \
  "CREATE DATABASE IF NOT EXISTS fasor_hub_test CHARACTER SET utf8mb4; GRANT ALL ON fasor_hub_test.* TO 'fasor'@'%';"
mvn test
```

## Configuration

Settings come from environment variables. Copy `.env.example` as a starting point and never commit real values.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | built from the values below | Full JDBC URL, takes precedence when set |
| `DB_HOST` | `127.0.0.1` | Database host |
| `DB_PORT` | `3308` | Database port |
| `DB_NAME` | `fasor_hub` | Database name |
| `DB_USER` | `fasor` | Database user |
| `DB_PASSWORD` | `fasor` | Database password |
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` | unset | Used when the matching `DB_*` value is not set, so hosts such as Railway work without extra setup |
| `DB_POOL` | `8` | Maximum pooled connections |
| `SEED_ON_START` | unset | Set to `true` to seed an empty database |

## Project structure

```text
src/main/java/app/fasorhub/
  dao/        SQL access, one class per table
  exception/  Errors that map to user-facing messages
  filter/     Login, role, and CSRF checks for every request
  model/      Records and coded enums
  service/    Transactions and locking rules
  util/       Connection pool, transactions, clock, messages, seeder, startup tasks
  web/        Thin servlets, one per page
  web/manage/ Servlets for the manager pages
src/main/resources/
  schema.sql              Table definitions
  messages_en.properties  All user-facing text
src/main/webapp/
  WEB-INF/jsp/   Page templates
  WEB-INF/tags/  Shared layout, calendar, badge, and empty-state tags
  static/        CSS, JavaScript, and self-hosted fonts
src/test/java/   Service, constraint, seed, security, and concurrency tests
docs/            Use cases, design documents, and the design system
Dockerfile       Builds the WAR and runs it on Tomcat
```

## Design notes

- All booking changes run in one transaction. The booking row is locked first, then its sessions in ascending id order, so concurrent actions cannot deadlock.
- "Today" always means the Asia/Jakarta time zone.
- User-facing text lives only in the message catalog, never in code.
