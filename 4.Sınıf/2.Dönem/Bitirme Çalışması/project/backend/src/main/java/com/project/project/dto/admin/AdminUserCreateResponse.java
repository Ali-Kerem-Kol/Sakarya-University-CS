package com.project.project.dto.admin;

/**
 * Response payload for admin account creation.
 */
public record AdminUserCreateResponse(
        Long id,
        String email,
        String role,
        boolean enabled,
        boolean emailVerified,
        String firstName,
        String lastName
) {
}
