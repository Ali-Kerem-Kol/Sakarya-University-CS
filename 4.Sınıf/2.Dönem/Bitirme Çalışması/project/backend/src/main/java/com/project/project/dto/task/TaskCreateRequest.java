package com.project.project.dto.task;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Admin task create request supporting assign mode.
 */
public record TaskCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 5000) String description,
        LocalDate dueDate,
        TaskAssignMode assignMode,
        Long assigneeUserId,
        TaskScope scope,
        Long assignedToUserId
) {

    public TaskAssignMode resolvedAssignMode() {
        if (assignMode != null) {
            return assignMode;
        }
        if (scope == null) {
            return null;
        }
        return scope == TaskScope.USER ? TaskAssignMode.USER : TaskAssignMode.ALL;
    }

    public Long resolvedAssigneeUserId() {
        return assigneeUserId != null ? assigneeUserId : assignedToUserId;
    }
}
