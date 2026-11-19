# CodeGraph Report - OfficeCollaboration

Generated 2026-11-19 with `codegraph 1.6.3` at commit `dd04d4a`. Regenerate: `codegraph index . && codegraph status .`

## Index status
```

CodeGraph Status

Project: .

Index Statistics:
  Files:     45
  Nodes:     660
  Edges:     921
  DB Size:   1.67 MB
  Backend:   node:sqlite — built-in (full WAL)
  Journal:   wal

Nodes by Kind:
  import          235
  method          127
  field           66
  file            43
  constant        40
  property        31
  function        29
  namespace       27
  class           25
  interface       12
  route           11
  enum_member     7
  type_alias      5
  enum            2

Files by Language:
  java            27
  tsx             9
  typescript      4
  javascript      2  (0 with symbols)
  yaml            2
  xml             1

⚠ No symbols came from any javascript file — fine if they hold no code, otherwise its parser isn't working: frontend/postcss.config.js, frontend/tailwind.config.js

✓ Index is up to date

```

## Circular imports
```
✓ No circular imports found
```

## Impact of changing `DocumentService`
```

Impact of changing "DocumentService" — 2 distinct definitions (each with its own blast radius; narrow with --file):

class com.officecollab.documentcore.service::DocumentService (java) — backend/src/main/java/com/officecollab/documentcore/service/DocumentService.java:26 — 44 affected symbols:

backend/src/main/java/com/officecollab/documentcore/service/DocumentService.java
  class       DocumentService:26
  class       DocumentAndVersion:162
  field       version:162
  field       permission:162
  field       document:162
  method      document:162
  method      version:162
  method      permission:162
  method      loadForEditorConfig:166
  field       documentVersionRepository:30
  field       documentRepository:29
  field       s3StorageService:32
  field       documentPermissionRepository:31
  method      createFromUpload:48
  method      createFromTemplate:69
  method      toDto:181
  method      getDetail:153
  method      createDocument:87
  method      rename:139
  method      DocumentService:34
  method      listForUser:44
  method      requirePermission:175

backend/src/main/java/com/officecollab/documentcore/controller/OnlyOfficeCallbackController.java
  method      content:63
  route       GET /api/v1/onlyoffice/content:63
  namespace   com.officecollab.documentcore.controller:1
  field       documentService:44
  method      OnlyOfficeCallbackController:47

backend/src/main/java/com/officecollab/documentcore/service/EditorConfigService.java
  method      buildSignedConfig:29
  field       documentService:17
  method      EditorConfigService:21
```

## Impact of changing `OnlyOfficeCallbackService`
```

Impact of changing "OnlyOfficeCallbackService" — 2 distinct definitions (each with its own blast radius; narrow with --file):

class com.officecollab.documentcore.service::OnlyOfficeCallbackService (java) — backend/src/main/java/com/officecollab/documentcore/service/OnlyOfficeCallbackService.java:30 — 16 affected symbols:

backend/src/main/java/com/officecollab/documentcore/service/OnlyOfficeCallbackService.java
  class       OnlyOfficeCallbackService:30
  constant    STATUS_READY_FOR_SAVING:34
  method      handleCallback:53
  constant    log:33
  constant    STATUS_SAVING_ERROR_FORCE_SAVE:35
  field       httpClient:40
  field       documentVersionRepository:38
  field       documentRepository:37
  field       s3StorageService:39
  method      OnlyOfficeCallbackService:42
  method      downloadFile:102

backend/src/main/java/com/officecollab/documentcore/controller/OnlyOfficeCallbackController.java
  method      callback:72
  route       POST /api/v1/onlyoffice/callback:72
  namespace   com.officecollab.documentcore.controller:1
  field       onlyOfficeCallbackService:43
  method      OnlyOfficeCallbackController:47


method com.officecollab.documentcore.service::OnlyOfficeCallbackService::OnlyOfficeCallbackService (java) — backend/src/main/java/com/officecollab/documentcore/service/OnlyOfficeCallbackService.java:42 — 1 affected symbols:

backend/src/main/java/com/officecollab/documentcore/service/OnlyOfficeCallbackService.java
  method      OnlyOfficeCallbackService:42

```

## Impact of changing `S3StorageService`
```

Impact of changing "S3StorageService" — 2 distinct definitions (each with its own blast radius; narrow with --file):

class com.officecollab.documentcore.service::S3StorageService (java) — backend/src/main/java/com/officecollab/documentcore/service/S3StorageService.java:16 — 23 affected symbols:

backend/src/main/java/com/officecollab/documentcore/service/S3StorageService.java
  class       S3StorageService:16
  constant    PRESIGNED_URL_TTL:19
  method      presignedGetUrl:47
  field       s3Presigner:22
  field       s3Client:21
  field       bucket:23
  method      upload:33
  method      download:65
  method      S3StorageService:25

backend/src/main/java/com/officecollab/documentcore/service/OnlyOfficeCallbackService.java
  method      handleCallback:53
  field       s3StorageService:39
  method      OnlyOfficeCallbackService:42

backend/src/main/java/com/officecollab/documentcore/controller/OnlyOfficeCallbackController.java
  method      callback:72
  method      content:63
  route       GET /api/v1/onlyoffice/content:63
  namespace   com.officecollab.documentcore.controller:1
  field       s3StorageService:45
  method      OnlyOfficeCallbackController:47

backend/src/main/java/com/officecollab/documentcore/service/DocumentService.java
  method      createDocument:87
  method      createFromUpload:48
  method      createFromTemplate:69
  field       s3StorageService:32
  method      DocumentService:34


method com.officecollab.documentcore.service::S3StorageService::S3StorageService (java) — backend/src/main/java/com/officecollab/documentcore/service/S3StorageService.java:25 — 1 affected symbols:

backend/src/main/java/com/officecollab/documentcore/service/S3StorageService.java
```

## File structure
```

Project Structure (45 files):

├── backend
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── officecollab
│   │       │           └── documentcore
│   │       │               ├── config
│   │       │               │   ├── S3Config.java (java, 14 symbols)
│   │       │               │   └── SecurityConfig.java (java, 11 symbols)
│   │       │               ├── controller
│   │       │               │   ├── ApiExceptionHandler.java (java, 13 symbols)
│   │       │               │   ├── DocumentController.java (java, 39 symbols)
│   │       │               │   └── OnlyOfficeCallbackController.java (java, 37 symbols)
│   │       │               ├── domain
│   │       │               │   ├── Document.java (java, 39 symbols)
│   │       │               │   ├── DocumentPermission.java (java, 23 symbols)
│   │       │               │   ├── DocumentPermissionId.java (java, 20 symbols)
│   │       │               │   ├── DocumentVersion.java (java, 42 symbols)
│   │       │               │   └── PermissionLevel.java (java, 12 symbols)
│   │       │               ├── dto
│   │       │               │   ├── DocumentDto.java (java, 23 symbols)
│   │       │               │   ├── DocumentListItemDto.java (java, 15 symbols)
│   │       │               │   ├── NewDocumentRequest.java (java, 13 symbols)
│   │       │               │   └── RenameDocumentRequest.java (java, 6 symbols)
│   │       │               ├── repository
│   │       │               │   ├── DocumentPermissionRepository.java (java, 9 symbols)
│   │       │               │   ├── DocumentRepository.java (java, 11 symbols)
│   │       │               │   └── DocumentVersionRepository.java (java, 10 symbols)
│   │       │               ├── security
│   │       │               │   └── LocalUser.java (java, 7 symbols)
│   │       │               ├── service
│   │       │               │   ├── DocumentService.java (java, 45 symbols)
│   │       │               │   ├── EditorConfigService.java (java, 17 symbols)
│   │       │               │   ├── ForbiddenException.java (java, 4 symbols)
│   │       │               │   ├── NotFoundException.java (java, 4 symbols)
│   │       │               │   ├── OnlyOfficeCallbackService.java (java, 30 symbols)
│   │       │               │   ├── OnlyOfficeDocumentTypes.java (java, 14 symbols)
│   │       │               │   ├── OnlyOfficeJwtService.java (java, 19 symbols)
│   │       │               │   └── S3StorageService.java (java, 22 symbols)
│   │       │               └── DocumentCoreApplication.java (java, 6 symbols)
│   │       └── resources
│   │           └── application.yml (yaml, 15 symbols)
│   └── pom.xml (xml, 1 symbols)
├── frontend
│   ├── src
│   │   ├── components
│   │   │   ├── DocumentCard.tsx (tsx, 10 symbols)
│   │   │   ├── DocumentRow.tsx (tsx, 10 symbols)
│   │   │   ├── InlineRenameLabel.tsx (tsx, 11 symbols)
│   │   │   ├── NewDocumentMenu.tsx (tsx, 10 symbols)
│   │   │   └── Sidebar.tsx (tsx, 13 symbols)
│   │   ├── lib
│   │   │   ├── api.ts (typescript, 20 symbols)
│   │   │   └── fileType.ts (typescript, 14 symbols)
│   │   ├── pages
│   │   │   ├── DocumentList.tsx (tsx, 18 symbols)
│   │   │   └── Editor.tsx (tsx, 9 symbols)
│   │   ├── App.tsx (tsx, 8 symbols)
│   │   ├── main.tsx (tsx, 5 symbols)
│   │   └── vite-env.d.ts (typescript, 6 symbols)
│   ├── postcss.config.js (javascript, 1 symbols)
│   ├── tailwind.config.js (javascript, 1 symbols)
│   └── vite.config.ts (typescript, 3 symbols)
└── docker-compose.yml (yaml, 0 symbols)

```
