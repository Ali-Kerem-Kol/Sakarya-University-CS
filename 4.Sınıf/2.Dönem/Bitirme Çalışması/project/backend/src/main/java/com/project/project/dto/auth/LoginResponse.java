package com.project.project.dto.auth;

import java.time.Instant;

/**
 * Returns JWT access token details after successful authentication.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        Long userId,
        String email,
        String role
) {
}
