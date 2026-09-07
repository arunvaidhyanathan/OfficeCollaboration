package com.officecollab.documentcore.controller;

import com.officecollab.documentcore.dto.DocumentDto;
import com.officecollab.documentcore.dto.DocumentListItemDto;
import com.officecollab.documentcore.dto.NewDocumentRequest;
import com.officecollab.documentcore.dto.RenameDocumentRequest;
import com.officecollab.documentcore.security.LocalUser;
import com.officecollab.documentcore.service.DocumentService;
import com.officecollab.documentcore.service.EditorConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final EditorConfigService editorConfigService;

    public DocumentController(DocumentService documentService, EditorConfigService editorConfigService) {
        this.documentService = documentService;
        this.editorConfigService = editorConfigService;
    }

    @GetMapping
    public List<DocumentListItemDto> list() {
        return documentService.listForUser(LocalUser.ID);
    }

    @PostMapping(consumes = "multipart/form-data")
    public DocumentDto upload(@RequestParam("file") MultipartFile file,
                               @RequestParam("name") String name) {
        return documentService.createFromUpload(LocalUser.ID, file, name);
    }

    @PostMapping("/new")
    public DocumentDto createFromTemplate(@Valid @RequestBody NewDocumentRequest request) {
        return documentService.createFromTemplate(LocalUser.ID, request);
    }

    @GetMapping("/{id}")
    public DocumentDto get(@PathVariable("id") UUID id) {
        return documentService.getDetail(LocalUser.ID, id);
    }

    @PatchMapping("/{id}")
    public DocumentDto rename(@PathVariable("id") UUID id, @Valid @RequestBody RenameDocumentRequest request) {
        return documentService.rename(LocalUser.ID, id, request.name());
    }

    @GetMapping("/{id}/editor-config")
    public ResponseEntity<Map<String, Object>> editorConfig(@PathVariable("id") UUID id) {
        Map<String, Object> config = editorConfigService.buildSignedConfig(LocalUser.ID, id, LocalUser.NAME);
        return ResponseEntity.ok(config);
    }
}
