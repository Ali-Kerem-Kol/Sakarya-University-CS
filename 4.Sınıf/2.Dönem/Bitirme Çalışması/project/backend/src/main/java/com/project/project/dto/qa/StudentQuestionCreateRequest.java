package com.project.project.dto.qa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Student question create request with posting reference.
 */
public record StudentQuestionCreateRequest(
        @NotNull Long postingId,
        @NotBlank @Size(max = 5000) String questionText
) {
}

