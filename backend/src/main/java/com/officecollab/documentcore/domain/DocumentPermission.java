package com.officecollab.documentcore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Maps to officecollab.document_permissions. permission is one of owner/edit/comment/view (DB-enforced). */
@Entity
@Table(name = "document_permissions", schema = "officecollab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentPermission {

    @EmbeddedId
    private DocumentPermissionId id;

    @Column(nullable = false, length = 20)
    private String permission;

    @Column(name = "granted_at", nullable = false)
    private OffsetDateTime grantedAt;
}
