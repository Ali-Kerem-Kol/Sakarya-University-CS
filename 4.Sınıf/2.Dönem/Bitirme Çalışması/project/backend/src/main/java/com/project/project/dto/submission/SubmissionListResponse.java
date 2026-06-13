package com.project.project.dto.submission;

import java.util.List;

/**
 * List wrapper for submissions.
 */
public record SubmissionListResponse(List<SubmissionResponse> submissions) {
    public List<SubmissionResponse> getContent() {
        return submissions;
    }
}
