package com.project.project.dto.task;

/**
 * Timeline actor summary.
 */
public record TaskTimelineActorResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
