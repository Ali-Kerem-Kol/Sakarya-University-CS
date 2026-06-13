package com.project.project.dto.mail;

/**
 * Optional payload for mail job creation.
 */
public record MailJobCreateRequest(
        @jakarta.validation.constraints.NotBlank String subject,
        @jakarta.validation.constraints.NotBlank String body
) {
}
