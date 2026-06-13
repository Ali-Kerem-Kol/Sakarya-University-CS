package com.project.project.dto.submission;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationSubmissionStatus;

/**
 * Submission summary payload.
 */
public record SubmissionResponse(
        String id,
        String postingId,
        String postingTitle,
        ApplicationCategory postingCategory,
        String userId,
        @JsonProperty("createdAt")
        Instant submittedAt,
        ApplicationSubmissionStatus status
) {
}
