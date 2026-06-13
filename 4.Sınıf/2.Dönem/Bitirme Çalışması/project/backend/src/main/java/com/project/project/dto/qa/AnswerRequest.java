package com.project.project.dto.qa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin answer request.
 */
public record AnswerRequest(
        @NotBlank @Size(max = 5000) String answerText
) {
}

