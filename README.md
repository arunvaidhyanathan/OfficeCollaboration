# Office Collaboration — POC

Bare-minimum implementation per `docs/architecture/office-collaboration-architecture.html` §0.1: a
React/TS SPA, one Java backend ("Document Core"), ONLYOFFICE Document Server (Community, self-hosted),
nginx, and your existing Supabase Postgres project. No Kafka, no AI/Parsing service, no multi-tenancy —
all deliberately deferred to Phase 2.

## What's here

```
backend/    Spring Boot 3 / Java 21 "Document Core" service
frontend/   Vite + React 18 + TypeScript SPA
infra/      nginx config for the docker-compose stack
db/         reference copy of the schema already applied to Supabase (not executed by any app code)
docker-compose.yml
.env.example
```

## Assumption this whole build rests on

**Supabase Auth is the login mechanism** (frontend uses `@supabase/supabase-js`, backend validates the
resulting JWT). This was flagged as an open question and not yet confirmed — if you actually want a
different identity provider, the backend's `SecurityConfig`/JWKS wiring and the `auth.users(id)` foreign
keys in the schema both need to change before this is usable.

## Before you can run it

1. **Fill in `.env`** (copy `.env.example` → `.env` at this directory's root):
   - `ONLYOFFICE_JWT_SECRET` — any random string (`openssl rand -base64 32`).
   - `SPRING_DATASOURCE_PASSWORD` — your Supabase project's database password (Supabase dashboard →
     Settings → Database).
   - `AWS_S3_BUCKET`, `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` — an S3 bucket you
     control. **This bucket is not created for you** — create it first (any region; the app never
     provisions infrastructure).
2. **Create at least one Supabase Auth user** to log in with (Supabase dashboard → Authentication →
   Users → Add user, or just try the magic-link login once your own email is allowed — Supabase's
   default project settings allow open sign-up via magic link).
3. **Build the frontend once before first `docker compose up`** (nginx serves the static `dist/`
   output via a bind mount, it doesn't build it): `cd frontend && npm install && npm run build`.

## Running it

```bash
docker compose up --build
```

- App: http://localhost
- Document Core API directly: http://localhost:8080/api/v1
- ONLYOFFICE Document Server directly: http://localhost:8082

First login sends a Supabase magic link to your email; click it to come back authenticated.

## What was NOT verified in the sandbox this was built in

- **The backend was not compiled.** This sandbox's egress policy blocks Maven Central
  (`repo.maven.apache.org` etc. return 403), so `mvn package` couldn't run here. The code was written and
  manually reviewed against the Spring Security 6 / Spring Data JPA / JJWT 0.12.x / AWS SDK v2 APIs it
  uses, but **run `mvn -DskipTests package` yourself before trusting it** — this machine almost certainly
  has real Maven Central access (or a corporate mirror already configured).
- **The frontend was built successfully** (`npm install && npm run build` both passed) in the sandbox.
- **Nothing has been run end-to-end** — no real Supabase Auth login, no real S3 bucket, no real
  ONLYOFFICE container has actually exercised this code yet. Treat this as a first pass to run and debug,
  not a verified-working system.

## Local dev without the full docker-compose stack

Useful for iterating on one side at a time:
- Backend alone: `cd backend && mvn spring-boot:run` (needs `.env`'s vars as real environment variables,
  or a `application-local.yml`/IDE run config with them set — Spring Boot doesn't read `.env` files itself
  outside docker-compose's `env_file:`).
- Frontend alone: `cd frontend && npm run dev` (Vite dev server on :5173; `application.yml`'s
  `APP_CORS_ORIGIN` default already matches this).
- You'll still need a running ONLYOFFICE container for the editor to load —
  `docker compose up onlyoffice` on its own is enough for that piece.

## What's deliberately not built (Phase 2)

AI/Parsing service (Python), pgvector/embeddings, any message queue/outbox, multi-tenancy, sharing UI
beyond the creator's implicit `owner` permission, comment-only editor mode (collapsed into view mode for
now). See the ADR for the full list and the reasoning.
