package com.project.project.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Defines file validation, storage, and retrieval operations.
 */
public interface FileStorageService {

    void validateFile(DocumentType documentType, MultipartFile file);

    FileStorageResult storeFile(Long userId, DocumentType documentType, MultipartFile file);

    Resource loadFileAsResource(String storagePath);
}
