package com.project.project.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

import com.project.project.dto.user.DocumentResponse;
import com.project.project.dto.user.UserDocumentsResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserDocumentService;

/**
 * Exposes upload and download endpoints for user CV documents.
 */
@RestController
@RequestMapping("/api/v1/users/me/documents")
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    public UserDocumentController(UserDocumentService userDocumentService) {
        this.userDocumentService = userDocumentService;
    }

    @PostMapping("/cv")
    public ResponseEntity<DocumentResponse> uploadCv(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(userDocumentService.uploadCv(principal.getUserAccount().getId(), file));
    }

    @GetMapping
    public ResponseEntity<UserDocumentsResponse> listDocuments(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(userDocumentService.listDocuments(principal.getUserAccount().getId()));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long documentId
    ) {
        UserDocumentService.DocumentDownload download =
                userDocumentService.downloadDocument(principal.getUserAccount().getId(), documentId);
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
