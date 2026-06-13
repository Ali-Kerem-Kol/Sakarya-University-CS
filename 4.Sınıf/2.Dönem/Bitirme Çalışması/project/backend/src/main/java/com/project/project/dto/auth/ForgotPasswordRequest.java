package com.project.project.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for forgot password flow.
 */
public record ForgotPasswordRequest(@NotBlank @Email String email) {
}
