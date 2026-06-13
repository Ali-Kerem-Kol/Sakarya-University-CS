package com.project.project.service.storage;

/**
 * Carries stored file metadata for persistence.
 */
public record FileStorageResult(
        String originalFileName,
        String contentType,
        long size,
        String storageKey,
        String checksum
) {
}
