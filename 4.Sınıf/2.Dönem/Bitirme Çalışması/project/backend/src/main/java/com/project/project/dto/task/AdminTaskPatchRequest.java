package com.project.project.dto.task;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

/**
 * Partial update request for admin task actions.
 */
public record AdminTaskPatchRequest(
        @Size(max = 200) String title,
        @Size(max = 5000) String description,
        LocalDate dueDate,
        TaskScope scope,
        Long assignedToUserId,
        TaskGraphNodeStatus status
) {
}
