package com.project.project.dto.task;

import com.project.project.entity.TaskAssignmentStatus;

/**
 * Timeline assignment summary.
 */
public record TaskTimelineAssignmentResponse(
        Long id,
        TaskAssignmentStatus status
) {
}
