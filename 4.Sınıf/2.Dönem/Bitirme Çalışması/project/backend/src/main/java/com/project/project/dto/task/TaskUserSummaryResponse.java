package com.project.project.dto.task;

/**
 * User summary for task assignment payloads.
 */
public record TaskUserSummaryResponse(
        Long userId,
        String email,
        String firstName,
        String lastName
) {
}
