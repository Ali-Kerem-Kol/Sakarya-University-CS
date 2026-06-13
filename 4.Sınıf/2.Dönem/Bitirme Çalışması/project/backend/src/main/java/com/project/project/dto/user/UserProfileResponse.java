package com.project.project.dto.user;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Exposes user profile details to read operations.
 */
public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth,
        boolean hasCv,
        String cvFileName,
        Instant cvUploadedAt,
        String cvDownloadUrl
) {
}
