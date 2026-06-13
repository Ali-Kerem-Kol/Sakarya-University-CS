package com.project.project.dto.posting;

import java.time.Instant;
import java.util.List;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;

/**
 * Standard posting response payload.
 */
public record PostingResponse(
        Long id,
        ApplicationCategory category,
        String title,
        String description,
        String projectName,
        String projectDetails,
        ApplicationPostingStatus status,
        Instant publishedAt,
        Instant closedAt,
        Instant createdAt,
        List<AttachmentSummaryResponse> attachments
) {
}
