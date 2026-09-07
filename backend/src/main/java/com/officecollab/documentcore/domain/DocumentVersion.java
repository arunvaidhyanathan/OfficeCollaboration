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

/** Maps to officecollab.document_versions. See Document for why this isn't a JPA relationship to Document. */
@Entity
@Table(name = "document_versions", schema = "officecollab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersion {

    @Id
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    /**
     * MUST be deterministic per version (documentId + "_" + versionId) and NEVER regenerated on subsequent
     * editor-config requests for the same version -- ONLYOFFICE uses this key to match co-authoring sessions,
     * so a fresh key per request would fragment concurrent editors into separate sessions.
     */
    @Column(name = "onlyoffice_key", nullable = false, unique = true, length = 128)
    private String onlyofficeKey;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column
    private Long size;

    @Column(length = 128)
    private String checksum;
}
