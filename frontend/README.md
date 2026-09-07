# OfficeCollab Frontend (POC)

A bare-minimum in-browser office document collaboration frontend: Login, a
Document List, and an in-page ONLYOFFICE editor.

## Stack

- Vite + React 18 + TypeScript
- `react-router-dom` for client-side routing
- `@supabase/supabase-js` for auth (magic link / OTP) and Realtime presence
- `@onlyoffice/document-editor-react` to embed the ONLYOFFICE Document Server editor

## Setup

```bash
cp .env.example .env   # defaults already point at the shared POC Supabase project
npm install
npm run dev
```

## Environment variables

| Variable | Purpose |
| --- | --- |
| `VITE_SUPABASE_URL` | Supabase project URL used for auth + realtime presence |
| `VITE_SUPABASE_ANON_KEY` | Supabase anon/public key (safe to ship in the frontend bundle) |
| `VITE_API_BASE_URL` | Base URL of the "Document Core" Java backend, e.g. `http://localhost:8080/api/v1` |
| `VITE_ONLYOFFICE_URL` | Public URL of the ONLYOFFICE Document Server container |

## How it works

- **Auth**: Login screen only collects an email and calls
  `supabase.auth.signInWithOtp`. Supabase handles the magic-link redirect and
  session pickup automatically (`detectSessionInUrl`); `AuthContext` listens
  via `getSession()` + `onAuthStateChange` and `ProtectedRoute` redirects to
  `/login` when there is no session.
- **Document Core**: all non-auth calls (`src/lib/api.ts`) attach the current
  Supabase session's `access_token` as a Bearer token and hit
  `VITE_API_BASE_URL`.
- **Document List** (`/`): lists documents, and supports uploading a file or
  creating a blank docx/xlsx/pptx, then navigates to the new document's editor.
- **Editor** (`/documents/:id`): fetches an ONLYOFFICE editor config from
  Document Core (which includes a backend-signed `token`) and renders
  `<DocumentEditor>` from `@onlyoffice/document-editor-react`. A Supabase
  Realtime Presence channel (`presence:document:<id>`) tracks who else has the
  document open and renders their initials in the header - this is a pure
  Supabase Realtime feature with no backend involvement.

## Simplifications / TODOs for this POC

- The ONLYOFFICE editor config type is intentionally loose
  (`Record<string, unknown>`) - it is passed through to `<DocumentEditor>`
  as-is rather than fully modeled.
- No retry/backoff on API calls; errors are surfaced inline as plain text.
- No design system - inline styles and a tiny `index.css` only.
- No tests, no state management library, no dark mode, no multi-tenant UI.
- The Dockerfile builds and exports `dist/` only; nginx (part of the
  top-level infra, not this repo) is expected to serve those static files
  directly via a bind mount or `COPY --from=` in its own image build.
