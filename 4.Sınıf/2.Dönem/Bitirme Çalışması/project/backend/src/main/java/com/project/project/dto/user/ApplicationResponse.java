package com.project.project.dto.user;

import java.time.Instant;

import com.project.project.entity.UserApplicationStatus;

/**
 * Exposes application details for responses.
 */
public record ApplicationResponse(
        Long id,
        Long userId,
        String positionKey,
        UserApplicationStatus status,
        String motivationText,
        Instant createdAt,
        Instant lastStatusChangedAt
) {
}
