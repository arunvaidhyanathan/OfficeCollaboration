package com.officecollab.documentcore.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of PATCH /api/v1/documents/{id} -- rename a document. */
public record RenameDocumentRequest(
        @NotBlank String name
) {
}
