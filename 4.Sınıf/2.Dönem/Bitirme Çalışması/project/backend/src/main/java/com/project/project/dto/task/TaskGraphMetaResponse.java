package com.project.project.dto.task;

import java.time.LocalDate;

/**
 * Optional node metadata.
 */
public record TaskGraphMetaResponse(
        LocalDate dueDate,
        boolean hasSubmission,
        boolean hasReview
) {
}
