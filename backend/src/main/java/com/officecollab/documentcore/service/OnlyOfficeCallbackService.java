package com.officecollab.documentcore.service;

import com.officecollab.documentcore.domain.Document;
import com.officecollab.documentcore.domain.DocumentVersion;
import com.officecollab.documentcore.repository.DocumentRepository;
import com.officecollab.documentcore.repository.DocumentVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the ONLYOFFICE Document Server "save" callback business logic, once the caller (the controller)
 * has already verified the callback's JWT. See ONLYOFFICE's callback status docs: 2 = "document is ready for
 * saving" (editing session finished), 3 = "document saving error has occurred but the changes should still be
 * saved" (force-saved) -- both mean "fetch document.url and persist it as a new version". Other statuses
 * (1 = being edited, 4 = closed with no changes, 6/7 = force-save variants we don't distinguish here) are just
 * acknowledged for this bare-minimum POC.
 */
@Service
public class OnlyOfficeCallbackService {

    private static final Logger log = LoggerFactory.getLogger(OnlyOfficeCallbackService.class);
    private static final int STATUS_READY_FOR_SAVING = 2;
    private static final int STATUS_SAVING_ERROR_FORCE_SAVE = 3;

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final S3StorageService s3StorageService;
    private final HttpClient httpClient;

    public OnlyOfficeCallbackService(DocumentRepository documentRepository,
                                      DocumentVersionRepository documentVersionRepository,
                                      S3StorageService s3StorageService) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.s3StorageService = s3StorageService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Transactional
    public void handleCallback(UUID documentId, Map<String, Object> body) {
        int status = ((Number) body.getOrDefault("status", -1)).intValue();
        if (status != STATUS_READY_FOR_SAVING && status != STATUS_SAVING_ERROR_FORCE_SAVE) {
            log.debug("Ignoring ONLYOFFICE callback for document {} with status {}", documentId, status);
            return;
        }

        String downloadUrl = (String) body.get("url");
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalArgumentException("Callback status " + status + " had no document url to save from");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));
        DocumentVersion previousVersion = documentVersionRepository
                .findTopByDocumentIdOrderByVersionNumberDesc(documentId)
                .orElseThrow(() -> new NotFoundException("No existing version for document: " + documentId));

        byte[] fileBytes = downloadFile(downloadUrl);

        int newVersionNumber = previousVersion.getVersionNumber() + 1;
        UUID newVersionId = UUID.randomUUID();
        String filename = document.getName();
        String newStorageKey = "documents/%s/v%d/%s".formatted(documentId, newVersionNumber, filename);
        // Freshly generated for the NEW version only -- the old version's onlyoffice_key is left untouched.
        String newOnlyofficeKey = documentId + "_" + newVersionId;
        OffsetDateTime now = OffsetDateTime.now();

        s3StorageService.upload(newStorageKey, fileBytes, document.getMimeType());

        DocumentVersion newVersion = DocumentVersion.builder()
                .id(newVersionId)
                .documentId(documentId)
                .versionNumber(newVersionNumber)
                .storageKey(newStorageKey)
                .onlyofficeKey(newOnlyofficeKey)
                .createdBy(document.getCreatedBy())
                .createdAt(now)
                .size((long) fileBytes.length)
                .build();
        documentVersionRepository.save(newVersion);

        document.setCurrentVersionId(newVersionId);
        document.setStorageKey(newStorageKey);
        document.setUpdatedAt(now);
        documentRepository.save(document);
    }

    private byte[] downloadFile(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Failed to download saved document from ONLYOFFICE, HTTP " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to download saved document from " + url, e);
        }
    }
}
