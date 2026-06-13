package com.project.project.dto.submission;

import com.project.project.entity.ApplicationSubmissionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Admin-side request for manually adding an existing user into a project submission flow.
 */
public record AdminManualSubmissionRequest(
        @NotNull Long userId,
        ApplicationSubmissionStatus status
) {
    public ApplicationSubmissionStatus resolvedStatus() {
        return status != null ? status : ApplicationSubmissionStatus.APPROVED;
    }
}
