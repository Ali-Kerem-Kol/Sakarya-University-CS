package com.project.project.dto.qa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User question create request.
 */
public record QuestionCreateRequest(
        @NotBlank @Size(max = 5000) String questionText
) {
}
