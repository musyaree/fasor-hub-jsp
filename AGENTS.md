# Fasor Hub: project guide

A campus sports facility booking system (Java 21, Servlet + JSP + JSTL, JDBC, MySQL, Tomcat 10.1). Ported from the earlier Laravel version because the final exam requires JSP.

## Source of truth

- `docs/README.md` and `docs/DESIGN.md` (Indonesian). Update `docs/` together with the code when the design changes.

## Naming and language

- Code identifiers are English (see `docs/03-desain/penamaan.md`). The UI is English only.
- All message texts live in `src/main/resources/messages_en.properties` (keys `p01`-`p46`). Never hard-code message text in servlets or JSP.

## Architecture

- Servlets (when added) stay thin. Transactions and locking live in `service/BookingService` and `ScheduleService`; SQL lives in `dao/`.
- Locking order: the `bookings` row first, then `time_slots` rows ordered by `id`.
- "Today" uses Asia/Jakarta (`util/Clock`).
- Cross-cutting rules (login, role, CSRF) are in `filter/SecurityFilter`. Every POST form carries the hidden `_csrf` field.

## Workflow

- Tests use MySQL `fasor_hub_test`, not SQLite (no row locking). Run `mvn test`.
- Never commit `.env` or real passwords. Do not add Maven dependencies without team approval.
