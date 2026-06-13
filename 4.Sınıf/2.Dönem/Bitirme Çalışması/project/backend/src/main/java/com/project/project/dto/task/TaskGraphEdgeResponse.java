package com.project.project.dto.task;

/**
 * Graph edge.
 */
public record TaskGraphEdgeResponse(
        Long fromTaskId,
        Long toTaskId
) {
}
