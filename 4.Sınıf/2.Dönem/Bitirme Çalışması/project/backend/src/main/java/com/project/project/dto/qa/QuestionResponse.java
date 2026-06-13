package com.project.project.dto.qa;

import java.time.Instant;

import com.project.project.entity.PublishScope;

/**
 * Question + optional answer payload with anonymous asker.
 */
public record QuestionResponse(
        Long id,
        Long postingId,
        String questionText,
        String answerText,
        PublishScope publishScope,
        Instant createdAt
) {
}

