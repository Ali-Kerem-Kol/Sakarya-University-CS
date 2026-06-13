package com.project.project.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Carries input data for creating a new application.
 */
public record CreateApplicationRequest(
        @NotBlank @Size(max = 100) String positionKey,
        @Size(max = 2000) String motivationText
) {
}
