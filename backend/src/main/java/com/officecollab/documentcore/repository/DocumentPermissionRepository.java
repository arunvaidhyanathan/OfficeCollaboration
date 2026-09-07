package com.officecollab.documentcore.repository;

import com.officecollab.documentcore.domain.DocumentPermission;
import com.officecollab.documentcore.domain.DocumentPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, DocumentPermissionId> {

    Optional<DocumentPermission> findById_DocumentIdAndId_UserId(UUID documentId, UUID userId);
}
