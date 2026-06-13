package com.project.project.dto.submission;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmissionStatus;

/**
 * Row DTO for admin submission listing by category/posting filters.
 */
public record AdminSubmissionListItemResponse(
        @JsonProperty("id")
        Long submissionId,
        Long postingId,
        String postingTitle,
        ApplicationCategory postingCategory,
        ApplicationPostingStatus postingStatus,
        @JsonProperty("status")
        ApplicationSubmissionStatus submissionStatus,
        @JsonProperty("createdAt")
        Instant submittedAt,
        String userId,
        @JsonProperty("userEmail")
        String email,
        @JsonProperty("userFirstName")
        String firstName,
        @JsonProperty("userLastName")
        String lastName,
        Integer classYear,
        String department,
        String englishLevel,
        BigDecimal gpa,
        String cvDownloadUrl
) {
    @JsonProperty("user")
    public AdminSubmissionUserSummary user() {
        return new AdminSubmissionUserSummary(userId, firstName, lastName, email);
    }

    @JsonProperty("posting")
    public AdminSubmissionPostingSummary posting() {
        return new AdminSubmissionPostingSummary(String.valueOf(postingId), postingTitle, postingCategory, postingStatus);
    }

    public record AdminSubmissionUserSummary(String id, String firstName, String lastName, String email) {
    }

    public record AdminSubmissionPostingSummary(
            String id,
            String title,
            ApplicationCategory category,
            ApplicationPostingStatus status
    ) {
    }
}
