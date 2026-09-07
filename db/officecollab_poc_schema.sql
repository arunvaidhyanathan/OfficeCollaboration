-- Reference copy of the migration already applied to Supabase (project ref nrhsoabqeskybrznxfyi)
-- via the Supabase MCP as migration "officecollab_poc_schema". This file is NOT executed by any
-- app code or build step -- it's kept here purely so the schema is version-controlled alongside
-- the code that depends on it. If you ever need to reproduce it (a new environment, a second
-- Supabase project), run this by hand via the Supabase SQL editor or `supabase db execute`.
--
-- See docs/architecture/office-collaboration-architecture.html section 9 for the full rationale
-- (why a separate schema, why RLS-with-no-policies, why onlyoffice_key must be stable per version).

create schema if not exists officecollab;
comment on schema officecollab is 'Office Collaboration POC: document metadata + ACL. Binaries live in S3, not here.';

create table officecollab.documents (
    id                  uuid primary key default gen_random_uuid(),
    name                varchar(255) not null,
    mime_type           varchar(100) not null,
    storage_key         varchar(512) not null,
    current_version_id  uuid,
    created_by          uuid not null references auth.users(id),
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
    created_by      uuid not null references auth.users(id),
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
    user_id      uuid not null references auth.users(id),
    permission   varchar(20) not null check (permission in ('owner','edit','comment','view')),
    granted_at   timestamptz not null default now(),
    primary key (document_id, user_id)
);
comment on table officecollab.document_permissions is 'Per-document ACL. owner > edit > comment > view.';

create index document_permissions_user_id_idx on officecollab.document_permissions (user_id);

alter table officecollab.documents enable row level security;
alter table officecollab.document_versions enable row level security;
alter table officecollab.document_permissions enable row level security;

grant usage on schema officecollab to service_role, postgres;
grant all on all tables in schema officecollab to service_role, postgres;
