package com.project.project.dto.task;

import java.time.Instant;

/**
 * File metadata in task and submission responses.
 */
public record TaskFileResponse(
        Long id,
        String fileName,
        String contentType,
        long fileSizeBytes,
        Instant uploadedAt,
        String downloadUrl
) {
}
