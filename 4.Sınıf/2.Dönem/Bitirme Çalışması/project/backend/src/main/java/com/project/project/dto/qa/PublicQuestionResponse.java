package com.project.project.dto.qa;

import java.time.Instant;

/**
 * Public, anonymous Q/A response.
 */
public record PublicQuestionResponse(
        Long id,
        Long postingId,
        String questionText,
        String answerText,
        Instant publishedAt
) {
}

