package com.officecollab.documentcore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to officecollab.documents. Note: this is deliberately NOT wired up with a JPA @ManyToOne to
 * DocumentVersion (documents.current_version_id -> document_versions.id, while document_versions.document_id
 * points back to documents.id) to avoid a bidirectional/circular entity relationship for a bare-minimum POC.
 * currentVersionId is just a plain UUID column here; callers that need the full version row look it up via
 * DocumentVersionRepository.
 */
@Entity
@Table(name = "documents", schema = "officecollab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
