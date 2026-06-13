package com.project.project.dto.qa;

import java.time.Instant;

/**
 * Student-facing question response including own answer state.
 */
public record StudentQuestionResponse(
        Long id,
        Long postingId,
        String questionText,
        Instant createdAt,
        Instant answeredAt,
        String answerText,
        boolean isPublished
) {
}
