package com.project.project.dto.task;

import java.time.Instant;
import java.time.LocalDate;

import com.project.project.entity.TaskAssignmentStatus;

/**
 * List item projection for task assignment feeds.
 */
public record TaskAssignmentListItemResponse(
        Long assignmentId,
        Long taskId,
        Long projectId,
        String taskTitle,
        LocalDate dueDate,
        TaskAssignmentStatus status,
        Instant assignedAt,
        Instant submittedAt,
        Instant reviewedAt,
        TaskUserSummaryResponse assignee
) {
}
