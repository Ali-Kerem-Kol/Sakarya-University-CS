package com.project.project.dto.task;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
/**
 * Graph node.
 */
public record TaskGraphNodeResponse(
        Long taskId,
        String branchKey,
        Instant createdAt,
        String title,
        TaskGraphNodeStatus status,
        Long createdByUserId,
        Long assignedToUserId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String assignedToUserColor
) {
}
