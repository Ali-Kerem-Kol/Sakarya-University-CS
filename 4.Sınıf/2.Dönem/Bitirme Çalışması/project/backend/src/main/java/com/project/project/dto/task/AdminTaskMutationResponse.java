package com.project.project.dto.task;

import java.time.Instant;

/**
 * Minimal admin task mutation response.
 */
public record AdminTaskMutationResponse(
        Long taskId,
        Long projectId,
        String branchKey,
        TaskGraphNodeStatus status,
        Long assignedToUserId,
        Instant updatedAt
) {
}
