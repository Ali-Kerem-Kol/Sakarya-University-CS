package com.project.project.repository.projection;

import java.time.Instant;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmissionStatus;

/**
 * Flattened row projection for admin submission listing.
 */
public interface AdminSubmissionRowProjection {

    Long getSubmissionId();

    Long getPostingId();

    String getPostingTitle();

    ApplicationCategory getPostingCategory();

    ApplicationPostingStatus getPostingStatus();

    ApplicationSubmissionStatus getSubmissionStatus();

    Instant getSubmittedAt();

    Long getUserId();

    String getUserEmail();

    String getProfileFirstName();

    String getProfileLastName();

    String getProfileSnapshotJson();

    Long getCvDocumentIdSnapshot();
}
