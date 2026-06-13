package com.project.project.dto.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Admin review request for task/assignment status.
 */
public record AdminTaskReviewRequest(
        @NotNull TaskGraphNodeStatus status,
        @Size(max = 1000) String reviewNote,
        Long assignedToUserId
) {
}
