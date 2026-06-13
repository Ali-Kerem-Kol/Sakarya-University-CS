package com.project.project.dto.mail;

import java.time.Instant;

import com.project.project.entity.MailJobStatus;
import com.project.project.entity.MailJobType;

/**
 * Mail job response payload.
 */
public record MailJobResponse(
        Long id,
        MailJobType type,
        MailJobStatus status,
        String payloadJson,
        String errorMessage,
        Instant createdAt
) {
}
