package com.officecollab.documentcore.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Response body for GET /api/v1/documents/{id} and for document-creation endpoints. */
public record DocumentDto(
        UUID id,
        String name,
        String mimeType,
        UUID currentVersionId,
        int currentVersionNumber,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String permission
) {
}
