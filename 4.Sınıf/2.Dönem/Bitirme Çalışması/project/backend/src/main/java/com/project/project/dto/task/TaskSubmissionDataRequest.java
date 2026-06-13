package com.project.project.dto.task;

import jakarta.validation.constraints.Size;

/**
 * Multipart JSON part for student task submission.
 */
public record TaskSubmissionDataRequest(
        @Size(max = 5000) String textAnswer
) {
}
