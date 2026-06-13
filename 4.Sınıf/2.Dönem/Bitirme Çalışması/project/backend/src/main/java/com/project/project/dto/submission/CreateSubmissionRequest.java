package com.project.project.dto.submission;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for submitting to a posting.
 */
public record CreateSubmissionRequest(@NotNull Long postingId) {
}
