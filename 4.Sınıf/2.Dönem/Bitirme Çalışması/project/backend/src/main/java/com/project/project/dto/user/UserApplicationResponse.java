package com.project.project.dto.user;

import java.time.Instant;

import com.project.project.entity.UserApplicationStatus;

/**
 * Exposes user application details to read operations.
 */
public record UserApplicationResponse(
        Long id,
        String positionTitle,
        UserApplicationStatus status,
        Instant appliedAt
) {
}
