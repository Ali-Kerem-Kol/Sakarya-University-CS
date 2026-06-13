package com.project.project.dto.user;

import java.time.Instant;

/**
 * Exposes metadata for an uploaded user document.
 */
public record DocumentResponse(
        Long id,
        String type,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        Instant uploadedAt
) {
}
