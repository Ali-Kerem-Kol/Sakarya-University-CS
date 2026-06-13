package com.project.project.dto.qa;

import com.project.project.entity.PublishScope;

/**
 * Publish update request.
 */
public record PublishQuestionRequest(
        PublishScope scope,
        Boolean published
) {
}
