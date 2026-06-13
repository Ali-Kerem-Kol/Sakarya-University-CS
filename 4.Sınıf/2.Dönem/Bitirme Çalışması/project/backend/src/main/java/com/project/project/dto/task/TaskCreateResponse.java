package com.project.project.dto.task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Response for admin task creation workflow.
 */
public record TaskCreateResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        LocalDate dueDate,
        Instant createdAt,
        TaskUserSummaryResponse createdBy,
        List<TaskAssignmentListItemResponse> assignments
) {
}
