package com.project.project.dto.submission;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmissionStatus;

/**
 * Detailed admin view for a submission.
 */
public record AdminSubmissionDetailResponse(
        Long id,
        Long postingId,
        String postingTitle,
        ApplicationCategory postingCategory,
        ApplicationPostingStatus postingStatus,
        String userId,
        @JsonProperty("userEmail")
        String email,
        @JsonProperty("userFirstName")
        String firstName,
        @JsonProperty("userLastName")
        String lastName,
        @JsonProperty("createdAt")
        Instant submittedAt,
        ApplicationSubmissionStatus status,
        String profileSnapshotJson,
        Integer snapshotVersion,
        Long cvDocumentIdSnapshot,
        String cvDownloadUrl
) {
}
