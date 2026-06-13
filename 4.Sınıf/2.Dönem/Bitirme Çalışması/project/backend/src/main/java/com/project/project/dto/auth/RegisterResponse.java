package com.project.project.dto.auth;

/**
 * Returns basic account details after successful registration.
 */
public record RegisterResponse(Long userId, String email, String role) {
}
