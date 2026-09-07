package com.officecollab.documentcore.repository;

import com.officecollab.documentcore.domain.Document;
import com.officecollab.documentcore.dto.DocumentListItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Documents the given user has any permission on, newest-updated first, together with the user's
     * permission level for each -- backs GET /api/v1/documents. Document and DocumentPermission are not JPA
     * relationship-mapped to each other (see Document's javadoc), so this is an explicit multi-root JPQL join.
     */
    @Query("""
            select new com.officecollab.documentcore.dto.DocumentListItemDto(
                d.id, d.name, d.mimeType, d.updatedAt, p.permission
            )
            from Document d, DocumentPermission p
            where p.id.documentId = d.id
              and p.id.userId = :userId
            order by d.updatedAt desc
            """)
    List<DocumentListItemDto> findAccessibleDocuments(@Param("userId") UUID userId);
}
