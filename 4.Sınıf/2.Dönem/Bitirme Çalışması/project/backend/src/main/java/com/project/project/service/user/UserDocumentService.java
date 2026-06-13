package com.project.project.service.user;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.dto.user.DocumentResponse;
import com.project.project.dto.user.UserDocumentsResponse;

/**
 * Defines operations for user document uploads and retrievals.
 */
public interface UserDocumentService {

    DocumentResponse uploadCv(Long userId, MultipartFile file);

    UserDocumentsResponse listDocuments(Long userId);

    DocumentDownload downloadDocument(Long userId, Long documentId);

    UserDocumentsResponse listDocumentsForUser(Long userId);

    DocumentDownload downloadDocumentForUser(Long userId, Long documentId);

    /**
     * Provides resource metadata for file downloads.
     */
    record DocumentDownload(
            Resource resource,
            String contentType,
            String originalFileName
    ) {
    }
}
