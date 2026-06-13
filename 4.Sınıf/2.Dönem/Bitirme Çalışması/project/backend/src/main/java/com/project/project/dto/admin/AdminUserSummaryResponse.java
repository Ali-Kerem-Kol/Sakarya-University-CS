package com.project.project.dto.admin;

/**
 * Exposes summary fields for admin user listings.
 */
public record AdminUserSummaryResponse(
        Long id,
        String email,
        String role,
        boolean enabled,
        String preferredColor
) {
}
