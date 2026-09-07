package com.officecollab.documentcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Body of POST /api/v1/documents/new -- "create a blank document from a bundled template". */
public record NewDocumentRequest(
        @NotBlank String name,
        @NotNull TemplateType type
) {
    public enum TemplateType {
        docx, xlsx, pptx
    }
}
