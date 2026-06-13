package com.project.project.dto.task;

import jakarta.validation.constraints.Size;

/**
 * Optional note while changing assignment state.
 */
public record TaskAssignmentUpdateRequest(
        @Size(max = 1000) String note
) {
}

