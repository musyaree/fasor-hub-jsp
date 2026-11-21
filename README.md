# Fasor Hub (JSP port)

Campus sports facility booking. This branch is the Java port of the Laravel project, at the same progress as Laravel today:
the data foundation (S1.2) and `BookingService` approve/reject (S2.2). The remaining stories are still to be built.

Stack: **Java 21, Servlet 6 + JSP 3.1 + JSTL, JDBC (HikariCP), MySQL 8, Tomcat 10.1**, Maven.

## What exists

| Laravel | Java |
|---|---|
| migrations | `src/main/resources/schema.sql` (run by `AppInitializer`) |
| enums, models | `model/` (records and `Coded` enums, labels in `messages_en.properties`) |
| `lang/en/messages.php` | `src/main/resources/messages_en.properties` (p01-p33) |
| seeders | `util/Seeder` (set `SEED_ON_START=true` to seed an empty database) |
| `BookingService::approve`, `reject` | `service/BookingService` (done) |
| other `BookingService` and `ScheduleService` methods | stubs that throw `UnsupportedOperationException` |
| exceptions | `exception/` |
| tests | `src/test/java` (constraints, seeder, verification, concurrency) |

Not built yet: servlets, JSP views, filters, CSS/JS, auth, and the other services.

## Run the tests

1. MySQL 8 with the databases `fasor_hub` and `fasor_hub_test` (user `fasor`, password `fasor`, port 3308), for example:
   `docker run -d --name fasor-mysql -p 3308:3306 -e MYSQL_ROOT_PASSWORD=fasor -e MYSQL_DATABASE=fasor_hub -e MYSQL_USER=fasor -e MYSQL_PASSWORD=fasor mysql:8.0 --default-time-zone=+07:00`
   then create `fasor_hub_test` and grant `fasor` access to it.
2. `mvn test`

Settings come from environment variables (see `.env.example`). Demo accounts use the password `password`; never seed them on a public deployment.

## Design docs

`docs/` holds the use cases, data model, wireframes, messages and the UI direction (`docs/DESIGN.md`), in Indonesian.
