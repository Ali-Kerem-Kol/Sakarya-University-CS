package com.project.project.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Carries user credentials for JWT authentication.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
