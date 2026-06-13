package com.project.project.dto.posting;

/**
 * Exposes posting attachment metadata without leaking storage keys.
 */
public record AttachmentSummaryResponse(
        Long id,
        String fileName,
        String contentType,
        long size,
        String downloadUrl
) {
}
