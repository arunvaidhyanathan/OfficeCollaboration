# document-core

Backend service for the OfficeCollab POC ("Document Core"). It is the **sole** backend for this POC: it owns
Supabase Auth JWT validation, document metadata + ACL in Postgres, ONLYOFFICE editor config generation and
signing, the ONLYOFFICE save callback, and S3 presigned URLs. There is no other backend service (no
Python/AI service, no queue) in this POC.

## Running it

```
mvn spring-boot:run
```

or build and run the jar / Docker image (see `Dockerfile`). All configuration is via environment variables
(see table below); nothing is hardcoded.

## Endpoints (all under `/api/v1` unless noted)

All endpoints below require a valid Supabase user JWT (`Authorization: Bearer <token>`), validated against
Supabase's JWKS, **except** the ONLYOFFICE callback, which uses a completely separate signing scheme (see
below).

| Method | Path | Description |
|---|---|---|
| GET | `/documents` | Documents the caller has any permission on, newest-updated first, with the caller's permission level for each. |
| POST | `/documents` | multipart/form-data (`file`, `name`) — upload a new document. Creates the document, its v1 `document_versions` row, uploads to S3, and grants the uploader `owner`. |
| POST | `/documents/new` | JSON `{"name": string, "type": "docx"\|"xlsx"\|"pptx"}` — create a new document from one of the bundled blank templates instead of an upload. |
| GET | `/documents/{id}` | Document detail. 403 if the caller has no `document_permissions` row for it. |
| GET | `/documents/{id}/editor-config` | Builds and returns a signed ONLYOFFICE editor config: a short-TTL (10 min) S3 presigned URL for the current version, `documentType` derived from the file extension, `editorConfig.mode` = `edit` (owner/edit) or `view` (comment/view), and a `token` field = the whole config signed HS256 with `ONLYOFFICE_JWT_SECRET`. |
| POST | `/onlyoffice/callback?documentId={id}` | **Not** protected by the normal JWT filter chain (see `SecurityConfig`) — ONLYOFFICE Document Server calls this directly and can't present a user JWT. Instead, the controller verifies ONLYOFFICE's own JWT (from the `Authorization: Bearer` header, or the body's `token` field) against the same `ONLYOFFICE_JWT_SECRET`, and returns `403` with no DB/S3 side effects at all if that verification fails. On a verified callback with `status` 2 or 3, downloads `body.url`, stores it as a new version, and advances `documents.current_version_id`. Always responds `{"error": 0}` (success) or `{"error": 1}` (failure), per ONLYOFFICE's callback contract. |

## Environment variables

| Variable | Default | Notes |
|---|---|---|
| `SERVER_PORT` | `8080` | |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db.nrhsoabqeskybrznxfyi.supabase.co:5432/postgres` | Direct connection to the Supabase Postgres instance. If your network lacks IPv6 connectivity to the direct host, use the session-mode pooler instead: `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres` (with username `postgres.nrhsoabqeskybrznxfyi`). Do **not** point this at the transaction-mode pooler port (6543) — Hibernate needs prepared-statement support that mode doesn't provide. |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Use `postgres.nrhsoabqeskybrznxfyi` if using the pooler URL above. |
| `SPRING_DATASOURCE_PASSWORD` | *(none — required)* | From Supabase dashboard: Settings > Database. **TODO: fill in.** |
| `SUPABASE_JWKS_URI` | `https://nrhsoabqeskybrznxfyi.supabase.co/auth/v1/.well-known/jwks.json` | Primary JWT validation strategy. |
| `SUPABASE_JWT_SECRET` | *(none)* | Only used if this project turns out to still be on legacy HS256 JWT signing (the JWKS endpoint 404s at runtime). See the comment in `SecurityConfig` for the one-line swap to a shared-secret `JwtDecoder`. **TODO: fill in only if needed.** |
| `ONLYOFFICE_JWT_SECRET` | *(none — required for editor config / callback to work)* | Must match the `JWT_SECRET` configured on the ONLYOFFICE Document Server. **TODO: fill in.** |
| `AWS_S3_BUCKET` | *(none — required)* | Bucket used for all document version storage. **TODO: fill in.** |
| `AWS_REGION` | `us-east-1` | |
| `APP_CORS_ORIGIN` | `http://localhost:5173` | Browser origin allowed to call this API. |
| `APP_PUBLIC_BASE_URL` | `http://localhost:8080` | Used to build the `editorConfig.callbackUrl` ONLYOFFICE calls back on — must be reachable *from the ONLYOFFICE Document Server*, not just from a browser. |

AWS credentials are **not** an env var this app defines directly — the AWS SDK v2 default credential provider
chain is used (env vars `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`/`AWS_SESSION_TOKEN`, shared `~/.aws/config`,
or an instance/container role). **TODO: provide real AWS credentials** via whichever of those mechanisms fits
your deployment target.

## Deliberate simplifications for this POC (see task description for what's explicitly deferred)

- No Kafka/queue, no outbox table, no Python/AI service integration, no multi-tenancy, no
  embeddings/pgvector — all out of scope by design, not overlooked.
- ONLYOFFICE editor mode is binary (`edit` or `view` only) — `comment` permission maps to `view` mode for now;
  ONLYOFFICE's finer-grained comment-only permissions are not wired up.
- The callback handler acts on `status` 2 and 3 only; other statuses are acknowledged without any action.
- Document `mime_type` for uploads is taken from the multipart request's `Content-Type` header as supplied by
  the browser (falling back to `application/octet-stream`), not sniffed from file content.
- No virus/malware scanning of uploaded files.
- No pagination on `GET /documents` — fine for a POC's small document counts, would need it before real use.
- `document_permissions` beyond the creator's `owner` row are never granted anywhere in this code (no
  "share with another user" endpoint) — deferred, matches "bare-minimum POC" scope.

## Things a human still needs to supply

- `SPRING_DATASOURCE_PASSWORD` — from the Supabase dashboard (Settings > Database).
- `ONLYOFFICE_JWT_SECRET` — whatever secret you configure the ONLYOFFICE Document Server's `JWT_SECRET`
  environment variable to.
- `AWS_S3_BUCKET` plus real AWS credentials (env vars, shared config, or an instance/container role) for the
  default credential provider chain to pick up.
- `SUPABASE_JWT_SECRET` — only if you discover (JWKS 404 at runtime) that this Supabase project is still on
  legacy HS256 JWT signing; see the comment in `SecurityConfig`.
