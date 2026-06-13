package com.project.project.dto.user;

/**
 * Minimal account response for authenticated account updates.
 */
public record MyAccountResponse(
        Long userId,
        String email,
        String role
) {
}
