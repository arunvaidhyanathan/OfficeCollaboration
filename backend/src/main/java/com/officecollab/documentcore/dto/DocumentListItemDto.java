package com.officecollab.documentcore.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** One row of GET /api/v1/documents. Built via a JPQL constructor-expression projection, see DocumentRepository. */
public record DocumentListItemDto(
        UUID id,
        String name,
        String mimeType,
        OffsetDateTime updatedAt,
        String permission
) {
}
