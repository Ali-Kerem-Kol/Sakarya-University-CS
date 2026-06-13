package com.project.project.dto.user;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Carries editable user profile fields for update operations.
 */
public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 30) String phoneNumber,
        LocalDate dateOfBirth
) {
}
