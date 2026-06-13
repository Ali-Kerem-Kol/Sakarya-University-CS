package com.project.project.dto.task;

import java.time.Instant;

import com.project.project.entity.TaskAssignmentStatus;

/**
 * Task assignment payload.
 */
public record TaskAssignmentResponse(
        Long userId,
        TaskAssignmentStatus status,
        String note,
        Instant updatedAt
) {
}

