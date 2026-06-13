package com.project.project.dto.admin;

import java.time.Instant;

/**
 * Exposes admin note details attached to a user application.
 */
public record AdminNoteResponse(
        Long id,
        Long applicationId,
        String noteText,
        Instant createdAt,
        String createdByAdminEmail
) {
}
