package com.officecollab.documentcore.service;

import com.officecollab.documentcore.domain.Document;
import com.officecollab.documentcore.domain.DocumentPermission;
import com.officecollab.documentcore.domain.DocumentPermissionId;
import com.officecollab.documentcore.domain.DocumentVersion;
import com.officecollab.documentcore.domain.PermissionLevel;
import com.officecollab.documentcore.dto.DocumentDto;
import com.officecollab.documentcore.dto.DocumentListItemDto;
import com.officecollab.documentcore.dto.NewDocumentRequest;
import com.officecollab.documentcore.repository.DocumentPermissionRepository;
import com.officecollab.documentcore.repository.DocumentRepository;
import com.officecollab.documentcore.repository.DocumentVersionRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentPermissionRepository documentPermissionRepository;
    private final S3StorageService s3StorageService;

    public DocumentService(DocumentRepository documentRepository,
                            DocumentVersionRepository documentVersionRepository,
                            DocumentPermissionRepository documentPermissionRepository,
                            S3StorageService s3StorageService) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.documentPermissionRepository = documentPermissionRepository;
        this.s3StorageService = s3StorageService;
    }

    public List<DocumentListItemDto> listForUser(UUID userId) {
        return documentRepository.findAccessibleDocuments(userId);
    }

    @Transactional
    public DocumentDto createFromUpload(UUID userId, MultipartFile file, String name) {
        String filename = StringUtils.hasText(name) ? name : file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("A document name is required");
        }
        String extension = OnlyOfficeDocumentTypes.extensionOf(filename);
        String mimeType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }

        return createDocument(userId, filename, mimeType, extension, bytes);
    }

    @Transactional
    public DocumentDto createFromTemplate(UUID userId, NewDocumentRequest request) {
        String extension = request.type().name();
        String filename = request.name().toLowerCase().endsWith("." + extension)
                ? request.name()
                : request.name() + "." + extension;
        String mimeType = OnlyOfficeDocumentTypes.mimeTypeForTemplateExtension(extension);

        byte[] bytes;
        try (var in = new ClassPathResource("templates/blank." + extension).getInputStream()) {
            bytes = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load bundled template for type " + extension, e);
        }

        return createDocument(userId, filename, mimeType, extension, bytes);
    }

    private DocumentDto createDocument(UUID userId, String filename, String mimeType, String extension,
                                        byte[] bytes) {
        UUID documentId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        String storageKey = "documents/%s/v1/%s".formatted(documentId, filename);
        String onlyofficeKey = documentId + "_" + versionId;
        OffsetDateTime now = OffsetDateTime.now();

        s3StorageService.upload(storageKey, bytes, mimeType);

        // documents.current_version_id references document_versions.id, while document_versions.document_id
        // references documents.id -- a circular FK pair. Insert order must therefore be: the document row
        // with current_version_id left null, then the version row (which can now reference the document),
        // then an update setting the document's current_version_id. Never insert the document with a
        // current_version_id pointing at a version row that doesn't exist yet.
        Document document = Document.builder()
                .id(documentId)
                .name(filename)
                .mimeType(mimeType)
                .storageKey(storageKey)
                .currentVersionId(null)
                .createdBy(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();
        document = documentRepository.save(document);

        DocumentVersion version = DocumentVersion.builder()
                .id(versionId)
                .documentId(documentId)
                .versionNumber(1)
                .storageKey(storageKey)
                .onlyofficeKey(onlyofficeKey)
                .createdBy(userId)
                .createdAt(now)
                .size((long) bytes.length)
                .build();
        documentVersionRepository.save(version);

        document.setCurrentVersionId(versionId);
        document = documentRepository.save(document);

        DocumentPermission permission = DocumentPermission.builder()
                .id(new DocumentPermissionId(documentId, userId))
                .permission(PermissionLevel.OWNER.dbValue())
                .grantedAt(now)
                .build();
        documentPermissionRepository.save(permission);

        return toDto(document, version, PermissionLevel.OWNER.dbValue());
    }

    @Transactional
    public DocumentDto rename(UUID userId, UUID documentId, String newName) {
        DocumentPermission permission = requirePermission(userId, documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));
        document.setName(newName);
        document.setUpdatedAt(OffsetDateTime.now());
        document = documentRepository.save(document);
        DocumentVersion version = documentVersionRepository.findById(document.getCurrentVersionId())
                .orElseThrow(() -> new NotFoundException("Current version not found for document: " + documentId));
        return toDto(document, version, permission.getPermission());
    }

    /** 403s (rather than 404s) whenever the caller has no permission row, per the POC's access-control model. */
    public DocumentDto getDetail(UUID userId, UUID documentId) {
        DocumentPermission permission = requirePermission(userId, documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));
        DocumentVersion version = documentVersionRepository.findById(document.getCurrentVersionId())
                .orElseThrow(() -> new NotFoundException("Current version not found for document: " + documentId));
        return toDto(document, version, permission.getPermission());
    }

    public record DocumentAndVersion(Document document, DocumentVersion version, PermissionLevel permission) {
    }

    /** Loads what the editor-config builder needs, enforcing the same 403 access rule as getDetail. */
    public DocumentAndVersion loadForEditorConfig(UUID userId, UUID documentId) {
        DocumentPermission permission = requirePermission(userId, documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));
        DocumentVersion version = documentVersionRepository.findById(document.getCurrentVersionId())
                .orElseThrow(() -> new NotFoundException("Current version not found for document: " + documentId));
        return new DocumentAndVersion(document, version, PermissionLevel.fromDbValue(permission.getPermission()));
    }

    private DocumentPermission requirePermission(UUID userId, UUID documentId) {
        return documentPermissionRepository.findById_DocumentIdAndId_UserId(documentId, userId)
                .orElseThrow(() -> new ForbiddenException(
                        "User %s has no access to document %s".formatted(userId, documentId)));
    }

    private DocumentDto toDto(Document document, DocumentVersion version, String permission) {
        return new DocumentDto(
                document.getId(),
                document.getName(),
                document.getMimeType(),
                document.getCurrentVersionId(),
                version.getVersionNumber(),
                document.getCreatedBy(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                permission
        );
    }
}
