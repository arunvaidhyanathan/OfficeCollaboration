# Graph Report - OfficeCollaboration  (2026-11-19)

## Corpus Check
- 48 files · ~15,299 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 189 nodes · 225 edges · 28 communities (16 shown, 12 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 28 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `dd04d4a8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]

## God Nodes (most connected - your core abstractions)
1. `DocumentService` - 11 edges
2. `DocumentController` - 8 edges
3. `Office Collaboration — POC` - 8 edges
4. `fileTypeOf()` - 6 edges
5. `OfficeCollab Frontend (POC)` - 6 edges
6. `document-core` - 6 edges
7. `request()` - 5 edges
8. `OnlyOfficeCallbackController` - 5 edges
9. `S3StorageService` - 5 edges
10. `OnlyOfficeDocumentTypes` - 5 edges

## Surprising Connections (you probably didn't know these)
- `DocumentRow()` --calls--> `fileTypeOf()`  [EXTRACTED]
  frontend/src/components/DocumentRow.tsx → frontend/src/lib/fileType.ts
- `DocumentCard()` --calls--> `fileTypeOf()`  [EXTRACTED]
  frontend/src/components/DocumentCard.tsx → frontend/src/lib/fileType.ts
- `Editor()` --calls--> `downloadUrl()`  [EXTRACTED]
  frontend/src/pages/Editor.tsx → frontend/src/lib/api.ts

## Communities (28 total, 12 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.09
Nodes (8): allowsEdit(), dbValue(), fromDbValue(), Permission, DocumentPermissionRepository, DocumentService, EditorConfigService, OnlyOfficeDocumentTypes

### Community 1 - "Community 1"
Cohesion: 0.12
Nodes (18): DocumentCard(), DocumentCardProps, DocumentRow(), DocumentRowProps, InlineRenameLabelProps, ApiError, createBlankDocument(), DocumentSummary (+10 more)

### Community 2 - "Community 2"
Cohesion: 0.14
Nodes (10): NewDocumentMenuProps, FilterValue, NAV_ITEMS, SidebarProps, DOCX, FileKind, FileTypeInfo, OTHER (+2 more)

### Community 3 - "Community 3"
Cohesion: 0.17
Nodes (3): OnlyOfficeCallbackController, DocumentVersionRepository, OnlyOfficeCallbackService

### Community 5 - "Community 5"
Cohesion: 0.18
Nodes (10): Assumption this whole build rests on, Before you can run it, code:block1 (backend/    Spring Boot 3 / Java 21 "Document Core" service), code:bash (docker compose up --build), Local dev without the full docker-compose stack, Office Collaboration — POC, Running it, What's deliberately not built (Phase 2) (+2 more)

### Community 7 - "Community 7"
Cohesion: 0.25
Nodes (7): code:bash (cp .env.example .env   # defaults already point at the share), Environment variables, How it works, OfficeCollab Frontend (POC), Setup, Simplifications / TODOs for this POC, Stack

### Community 8 - "Community 8"
Cohesion: 0.25
Nodes (7): code:block1 (mvn spring-boot:run), Deliberate simplifications for this POC (see task description for what's explicitly deferred), document-core, Endpoints (all under `/api/v1` unless noted), Environment variables, Running it, Things a human still needs to supply

### Community 9 - "Community 9"
Cohesion: 0.29
Nodes (3): RuntimeException, ForbiddenException, NotFoundException

## Knowledge Gaps
- **33 isolated node(s):** `ImportMetaEnv`, `ImportMeta`, `InlineRenameLabelProps`, `DocumentRowProps`, `NewDocumentMenuProps` (+28 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Permission` connect `Community 0` to `Community 1`?**
  _High betweenness centrality (0.184) - this node is a cross-community bridge._
- **Why does `DocumentService` connect `Community 0` to `Community 4`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Why does `DocumentController` connect `Community 4` to `Community 0`, `Community 3`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **What connects `ImportMetaEnv`, `ImportMeta`, `InlineRenameLabelProps` to the rest of the system?**
  _33 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._