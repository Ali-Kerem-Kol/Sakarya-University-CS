package com.project.project.dto.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Admin review request for task assignment.
 */
public record TaskReviewRequest(
        @NotNull TaskReviewDecision decision,
        @Size(max = 1000) String note
) {
}
