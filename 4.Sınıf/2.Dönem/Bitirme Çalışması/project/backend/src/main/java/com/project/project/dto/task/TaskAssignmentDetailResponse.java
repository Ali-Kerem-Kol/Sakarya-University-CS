package com.project.project.dto.task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.project.project.entity.TaskAssignmentStatus;

/**
 * Detailed task assignment response for admin/student views.
 */
public record TaskAssignmentDetailResponse(
        Long assignmentId,
        Long taskId,
        Long projectId,
        String taskTitle,
        String taskDescription,
        LocalDate dueDate,
        TaskAssignmentStatus status,
        Instant assignedAt,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewNote,
        String textAnswer,
        TaskUserSummaryResponse assignee,
        TaskUserSummaryResponse createdBy,
        List<TaskFileResponse> taskAttachments,
        List<TaskFileResponse> submissionFiles
) {
}
