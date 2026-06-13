package com.project.project.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for authenticated email change.
 */
public record UpdateMyEmailRequest(
        @NotBlank @Email String email
) {
}
