package com.project.project.dto.qa;

import java.time.Instant;

/**
 * Admin-facing Q/A response with asker identity.
 */
public record AdminQuestionResponse(
        Long id,
        Long postingId,
        String postingTitle,
        String questionText,
        Instant createdAt,
        Long askedByUserId,
        String askedByEmail,
        String askedByName,
        String answerText,
        boolean isPublished,
        Instant publishedAt
) {
}
