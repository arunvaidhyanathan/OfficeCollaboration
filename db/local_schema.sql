-- Local-Postgres variant of officecollab_poc_schema.sql, for the docker-compose "db" service.
--
-- Differs from the Supabase original in two ways, both because this runs against a plain
-- postgres:15-alpine image that doesn't have Supabase's auth schema or roles:
--   1. created_by/user_id columns are plain uuid, not "references auth.users(id)" -- that table
--      only exists on Supabase (their hosted Auth service owns it). Auth/JWT verification still
--      goes through Supabase (see SUPABASE_JWKS_URI); only data storage is local.
--   2. Grants target "postgres" only -- "service_role" is a Supabase-managed role that doesn't
--      exist here.
--
-- Mounted read-only into /docker-entrypoint-initdb.d/, so postgres runs it once on first init.

create schema if not exists officecollab;
comment on schema officecollab is 'Office Collaboration POC: document metadata + ACL. Binaries live in S3, not here.';

create table officecollab.documents (
    id                  uuid primary key default gen_random_uuid(),
    name                varchar(255) not null,
    mime_type           varchar(100) not null,
    storage_key         varchar(512) not null,
    current_version_id  uuid,
    created_by          uuid not null,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);
comment on table officecollab.documents is 'Logical documents. Binary content lives in S3 at storage_key; this row is metadata only.';

create table officecollab.document_versions (
    id              uuid primary key default gen_random_uuid(),
    document_id     uuid not null references officecollab.documents(id) on delete cascade,
    version_number  int not null,
    storage_key     varchar(512) not null,
    onlyoffice_key  varchar(128) not null unique,
    created_by      uuid not null,
    created_at      timestamptz not null default now(),
    size            bigint,
    checksum        varchar(128),
    unique (document_id, version_number)
);
comment on table officecollab.document_versions is 'Append-only version history. onlyoffice_key must be stable per version, never regenerated per request.';

alter table officecollab.documents
  add constraint documents_current_version_fk
  foreign key (current_version_id) references officecollab.document_versions(id);

create index document_versions_document_id_idx on officecollab.document_versions (document_id);

create table officecollab.document_permissions (
    document_id  uuid not null references officecollab.documents(id) on delete cascade,
    user_id      uuid not null,
    permission   varchar(20) not null check (permission in ('owner','edit','comment','view')),
    granted_at   timestamptz not null default now(),
    primary key (document_id, user_id)
);
comment on table officecollab.document_permissions is 'Per-document ACL. owner > edit > comment > view.';

create index document_permissions_user_id_idx on officecollab.document_permissions (user_id);

alter table officecollab.documents enable row level security;
alter table officecollab.document_versions enable row level security;
alter table officecollab.document_permissions enable row level security;

grant usage on schema officecollab to postgres;
grant all on all tables in schema officecollab to postgres;
