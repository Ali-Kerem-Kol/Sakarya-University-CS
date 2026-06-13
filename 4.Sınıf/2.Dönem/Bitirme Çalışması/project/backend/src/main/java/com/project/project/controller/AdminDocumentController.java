package com.project.project.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;

import com.project.project.dto.user.UserDocumentsResponse;
import com.project.project.service.user.UserDocumentService;

/**
 * Exposes admin endpoints for listing and downloading user documents.
 */
@RestController
@RequestMapping("/api/v1/admin/users/{userId}/documents")
public class AdminDocumentController {

    private final UserDocumentService userDocumentService;

    public AdminDocumentController(UserDocumentService userDocumentService) {
        this.userDocumentService = userDocumentService;
    }

    @GetMapping
    public ResponseEntity<UserDocumentsResponse> listDocuments(@PathVariable Long userId) {
        return ResponseEntity.ok(userDocumentService.listDocumentsForUser(userId));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {
        UserDocumentService.DocumentDownload download =
                userDocumentService.downloadDocumentForUser(userId, documentId);
        String fileName = (download.originalFileName() == null || download.originalFileName().isBlank())
                ? "cv.pdf"
                : download.originalFileName();
        String contentDisposition = ContentDisposition.inline()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(download.resource());
    }
}
