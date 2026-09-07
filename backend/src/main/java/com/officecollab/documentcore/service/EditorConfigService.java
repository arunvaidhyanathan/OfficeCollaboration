package com.officecollab.documentcore.service;

import com.officecollab.documentcore.domain.Document;
import com.officecollab.documentcore.domain.DocumentVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Builds and signs the ONLYOFFICE editor config returned by GET /api/v1/documents/{id}/editor-config. */
@Service
public class EditorConfigService {

    private final DocumentService documentService;
    private final OnlyOfficeJwtService onlyOfficeJwtService;
    private final String publicBaseUrl;

    public EditorConfigService(DocumentService documentService,
                                OnlyOfficeJwtService onlyOfficeJwtService,
                                @Value("${app.public-base-url}") String publicBaseUrl) {
        this.documentService = documentService;
        this.onlyOfficeJwtService = onlyOfficeJwtService;
        this.publicBaseUrl = publicBaseUrl;
    }

    public Map<String, Object> buildSignedConfig(UUID userId, UUID documentId, String userName) {
        DocumentService.DocumentAndVersion loaded = documentService.loadForEditorConfig(userId, documentId);
        Document document = loaded.document();
        DocumentVersion version = loaded.version();

        String extension = OnlyOfficeDocumentTypes.extensionOf(document.getStorageKey());
        String documentType = OnlyOfficeDocumentTypes.documentTypeFor(extension);
        // Proxied through our own backend, not a direct S3 presigned URL -- see
        // OnlyOfficeCallbackController#content for why.
        String downloadUrl = publicBaseUrl + "/api/v1/onlyoffice/content?documentId=" + documentId;
        String mode = loaded.permission().allowsEdit() ? "edit" : "view";

        Map<String, Object> documentSection = new LinkedHashMap<>();
        documentSection.put("fileType", extension);
        // NEVER regenerate this -- it must exactly match the stored onlyoffice_key for this version so
        // concurrent editors land in the same ONLYOFFICE co-authoring session.
        documentSection.put("key", version.getOnlyofficeKey());
        documentSection.put("title", document.getName());
        documentSection.put("url", downloadUrl);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", userId.toString());
        user.put("name", userName);

        Map<String, Object> editorConfig = new LinkedHashMap<>();
        editorConfig.put("mode", mode);
        editorConfig.put("callbackUrl", publicBaseUrl + "/api/v1/onlyoffice/callback?documentId=" + documentId);
        editorConfig.put("user", user);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("document", documentSection);
        config.put("documentType", documentType);
        config.put("editorConfig", editorConfig);

        // ONLYOFFICE requires the token to be a signature over the exact same config object being returned,
        // so sign a copy of everything built so far before adding the token field itself.
        String token = onlyOfficeJwtService.sign(new HashMap<>(config));
        config.put("token", token);
        return config;
    }
}
