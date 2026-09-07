package com.officecollab.documentcore.controller;

import io.jsonwebtoken.JwtException;
import com.officecollab.documentcore.security.LocalUser;
import com.officecollab.documentcore.service.DocumentService;
import com.officecollab.documentcore.service.OnlyOfficeCallbackService;
import com.officecollab.documentcore.service.OnlyOfficeJwtService;
import com.officecollab.documentcore.service.S3StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * NOT protected by the normal Spring Security JWT resource-server filter chain (see SecurityConfig -- this
 * path is matched by a separate permitAll chain) because the caller here is the ONLYOFFICE Document Server,
 * not a logged-in browser user with a Supabase JWT. Instead, ONLYOFFICE signs its own JWT with
 * ${ONLYOFFICE_JWT_SECRET} and we verify it ourselves, by hand, right here -- before touching S3 or the DB.
 * This is a deliberate fix for a known vulnerability class: an unauthenticated/unverified callback endpoint
 * lets anyone POST a URL and overwrite any document.
 */
@RestController
@RequestMapping("/api/v1/onlyoffice")
public class OnlyOfficeCallbackController {

    private static final Logger log = LoggerFactory.getLogger(OnlyOfficeCallbackController.class);
    private static final Map<String, Object> OK = Map.of("error", 0);
    private static final Map<String, Object> FAILED = Map.of("error", 1);

    private final OnlyOfficeJwtService onlyOfficeJwtService;
    private final OnlyOfficeCallbackService onlyOfficeCallbackService;
    private final DocumentService documentService;
    private final S3StorageService s3StorageService;

    public OnlyOfficeCallbackController(OnlyOfficeJwtService onlyOfficeJwtService,
                                         OnlyOfficeCallbackService onlyOfficeCallbackService,
                                         DocumentService documentService,
                                         S3StorageService s3StorageService) {
        this.onlyOfficeJwtService = onlyOfficeJwtService;
        this.onlyOfficeCallbackService = onlyOfficeCallbackService;
        this.documentService = documentService;
        this.s3StorageService = s3StorageService;
    }

    /**
     * ONLYOFFICE fetches the source document from here rather than a direct S3 presigned URL, because
     * ONLYOFFICE attaches its own "Authorization: Bearer <jwt>" header to every outbound request it makes --
     * that collides with S3's query-string SigV4 auth and S3 rejects the download with 400. This endpoint
     * doesn't care about that extra header, so the conflict never happens.
     */
    @GetMapping("/content")
    public ResponseEntity<byte[]> content(@RequestParam("documentId") UUID documentId) {
        DocumentService.DocumentAndVersion loaded = documentService.loadForEditorConfig(LocalUser.ID, documentId);
        byte[] bytes = s3StorageService.download(loaded.version().getStorageKey());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(loaded.document().getMimeType()))
                .body(bytes);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestParam("documentId") UUID documentId,
                                                          @RequestHeader(value = "Authorization", required = false)
                                                          String authorizationHeader,
                                                          @RequestBody Map<String, Object> body) {
        String token = extractToken(authorizationHeader, body);
        if (token == null) {
            log.warn("Rejecting ONLYOFFICE callback for document {}: no JWT present", documentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FAILED);
        }
        try {
            onlyOfficeJwtService.verify(token);
        } catch (JwtException e) {
            log.warn("Rejecting ONLYOFFICE callback for document {}: JWT verification failed: {}",
                    documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FAILED);
        }

        try {
            onlyOfficeCallbackService.handleCallback(documentId, body);
            return ResponseEntity.ok(OK);
        } catch (Exception e) {
            log.error("Failed to process ONLYOFFICE callback for document {}", documentId, e);
            return ResponseEntity.ok(FAILED);
        }
    }

    /** ONLYOFFICE sends its signature either as an Authorization: Bearer header or a "token" body field. */
    private String extractToken(String authorizationHeader, Map<String, Object> body) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length());
        }
        Object bodyToken = body.get("token");
        if (bodyToken instanceof String s && StringUtils.hasText(s)) {
            return s;
        }
        return null;
    }
}
