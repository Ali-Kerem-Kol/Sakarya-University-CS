package com.project.project.dto.task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.project.project.entity.TaskStatus;

/**
 * Task payload.
 */
public record TaskResponse(
        Long id,
        Long postingId,
        String title,
        String description,
        LocalDate dueDate,
        TaskStatus status,
        Instant createdAt,
        List<TaskAssignmentResponse> assignments
) {
}

