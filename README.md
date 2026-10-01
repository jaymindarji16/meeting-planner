# Meeting Planner (Prototype)

A small meeting planner built as a take-home exercise. Users sign up, log in,
upload an avatar, create meetings, and invite participants. Participants can
see meetings they're invited to; only the organizer can delete them.

This is intentionally a prototype, not production code.

---

## Stack

- **Backend:** Java 17, Spring Boot 3.4.1, Spring Data JPA, H2 (file-backed), BCrypt
- **Frontend:** Angular 17+ (standalone components, signals), SCSS
- **Storage:** local filesystem for avatars

Repo layout:

    meeting-planner/
    ├── meeting-planner-backend/    Spring Boot service
    ├── meeting-planner-frontend/   Angular app
    ├── README.md
    └── AI_LOG.md

---

## Running locally

Two terminals.

### Backend

    cd meeting-planner-backend
    ./gradlew bootRun
    # Windows: .\gradlew.bat bootRun

The backend starts on `http://localhost:8080`. First run creates the H2 data
file under `./data/` and the avatar directory under `./uploads/`.

### Frontend

    cd meeting-planner-frontend
    npm install          # first time only
    ng serve

App opens on `http://localhost:4200`. A dev proxy (`proxy.conf.json`) forwards
`/api/*` to the backend, so there's no CORS setup needed.

### Quick smoke test
    Swagger : http://localhost:8080/swagger-ui/index.html#

    curl -X POST http://localhost:8080/api/auth/signup \
      -H "Content-Type: application/json" \
      -d '{"name":"Alice","email":"alice@example.com","password":"secret123"}'

Should return the created user as JSON.

---

## Running tests

Backend (JUnit 5):

    cd meeting-planner-backend
    ./gradlew test

HTML report at `build/reports/tests/test/index.html`.

Currently 22 backend tests .

---

## Design decisions

The interesting choices, with the reasoning behind each.

### Prototype auth via an `X-User-Id` header

Signup hashes the password with BCrypt and returns the user. Login verifies
the password and returns the user. The frontend stores the user object in
`localStorage` and an Angular HTTP interceptor attaches `X-User-Id` to every
request. Controllers read it from the header.

No JWT, no sessions, no Spring Security filter chain. A full security setup
is a lot of ceremony for prototype, and the exercise explicitly says
not to build production-grade. The trade-off (the header is trivially
spoofable) is documented under "Known limitations".

### `MeetingAccessPolicy` as a separate class

Visibility rules (who can view, edit, delete a meeting) live in their own
small `@Component` rather than being inlined in `MeetingService`. This is the
only abstraction I added on top of the obvious service-repository split, and
it earns its place: the rules are business policy, and the next feature that
touches them (public meetings, delegated organizers, department scoping)
becomes a one-file change. Everything else that looked extractable — file
storage, notifications, domain events — I deliberately left concrete, because
there's only one implementation and no second use case yet.

### `ParticipantStatus` enum in the schema, no endpoint yet

Every participant row has a status (`INVITED`, `ACCEPTED`, `DECLINED`,
`TENTATIVE`). There's no endpoint to change it — participants default to
`INVITED`. The column is there because RSVP is the first feature any real
meeting app adds, and adding a status column to a populated table later is
more disruptive than defining it now.

### Participant visibility is enforced at the service layer

`MeetingService.getVisible(id, userId)` looks up the meeting, loads the user,
and asks `MeetingAccessPolicy.canView(...)`. If it returns false, the response
is 403 — not 404. The alternative (silently 404-ing to hide existence) is a
defensible choice, but I opted for the simpler behaviour and noted it here.

`GET /api/meetings` returns only meetings the caller is organizer or
participant of. It runs two queries (organizer meetings, participant meetings)
and merges them in Java. A single JPQL query with a `left join` and an `OR`
condition looked cleaner on paper but was returning wrong results in
Hibernate 7

### Avatars stored on disk, path-traversal guarded

`FileStorageService` writes to `./uploads/avatars/user-{id}.{ext}` and stores
only the filename in the DB. Two guards:

1. Only `.png`, `.jpg`, `.jpeg` extensions are accepted.
2. `resolve(filename)` normalises the path and rejects anything that escapes
   the upload directory.

The second one is what stops `../../../etc/passwd`-style reads if the stored
filename ever becomes attacker-influenced. It isn't reachable today, but it's
the kind of guard that's cheap to add now and painful to retrofit later.

Replacing an avatar deletes the previous file for that user (any extension)
before writing the new one.

### Angular: standalone components, signals, no state library

Standalone components and function-based guards throughout. `AuthService`
exposes `current` as a signal and `isLoggedIn` as a `computed`. Route guards
(`authGuard`, `guestGuard`) protect the routing tree.

No NgRx, no reactive forms, no state library. The app has one piece of shared
state (the logged-in user) and one form per page. Adding Redux to that would
be ceremony without benefit. If the app grew a calendar view, undo, or
cross-page drafts, I'd reach for those tools.


---

## Testing philosophy

The tests focus on the parts of the app where correctness is non-obvious:

- **`UserServiceTest`** — hashing behaviour, duplicate email rejection,
  wrong password, unknown email
- **`MeetingServiceTest`** — visibility rules, participant deduplication,
  past-time rejection, unknown participant ids
- **`MeetingAccessPolicyTest`** — the full view/edit/delete matrix across
  organizer, participant, and outsider
- **`FileStorageServiceTest`** — extension validation, empty file rejection,
  avatar replacement cleanup, path traversal

What I skipped: getter tests, DB-mapping round-trips, and end-to-end UI tests.
Those are cheap to write and rarely catch real bugs. The tests above target
validation boundaries, access rules, and file handling — the three places a
prototype tends to break first.

---

## Assumptions

- Single tenant, local filesystem, no external services. Stated explicitly
  in the exercise brief, so I didn't build around them.
- "Participants" means people invited by the organizer. The organizer is
  stored separately on `Meeting` and isn't duplicated into the participants
  list, even if the organizer passes their own id.
- Time zones are ignored. `LocalDateTime` throughout, no `ZonedDateTime`.
  Fine for a local prototype.
- Avatar size limit of 2 MB is enforced only by Spring's multipart config.
  The frontend `accept="image/png,image/jpeg"` attribute is convenience, not
  validation.

---

## Known limitations

- **Auth is spoofable.** Anyone who knows a user id can impersonate them by
  setting `X-User-Id`. Fine for a prototype; not fine for anything else.
- **No RSVP endpoint.** `ParticipantStatus` exists in the schema but can't
  be changed via the API.
- **No meeting edit.** Organizer can create and delete; there's no update.
- **No pagination.** `GET /api/meetings` returns everything the user can see.
- **No password reset, no email verification, no account deletion.**
- **Past-time validation lives on the client.** The backend does reject
  `scheduledAt` in the past on create, but the error path is untested
  end-to-end through the controller.
- **Delete confirmation uses `confirm()`.** A native dialog, not a modal.
- **No rate limiting, no CSRF, no HTTPS.** Because there's no session to
  protect, CSRF is currently moot — but the moment auth becomes real, this
  needs revisiting.
- **Avatar upload checks extension, not content.** A `.png`-named file that
  isn't actually a PNG will be stored. File-sniffing (Apache Tika or similar)
  would catch it.
- **`GET /api/users` returns every user.** Fine for a prototype where you
  need to pick participants from a list; would need scoping in any real app.

---

## What I'd improve with more time

1. **Replace header auth with sessions or JWT.** Everything else in the auth
   story depends on this. Spring Security with `HttpOnly` session cookies is
   the smallest sane step; JWT only if the frontend needs to be stateless
   across origins.
2. **Build the RSVP endpoint.** `PATCH /api/meetings/{id}/participants/me`
   with a status body. The schema already supports it, and it turns
   `ParticipantStatus` from "unused column" into a real feature.
3. **Meeting edit.** Add `PUT /api/meetings/{id}` plus an `canEdit` check
   through the existing `MeetingAccessPolicy`.
4. **Fetch joins for the query.** The current two-query approach for
   `findAllForUser` is correct but chatty. A `left join fetch` would return
   everything in one round trip.
5. **Component tests for the create-meeting flow.** Submitting with a past
   date, missing title, or zero participants. Would exercise the same edges
   I currently test at the service layer, but through the real form.
6. **Custom modal for delete.** Replace `confirm()` so the confirmation can
   carry the meeting title and match the app's styling.

---

## AI usage

See `AI_LOG.md` for the prompts that materially shaped the design.