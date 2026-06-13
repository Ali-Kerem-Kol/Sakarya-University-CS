package com.project.project.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Carries user application creation input.
 */
public record UserApplicationRequest(
        @NotBlank @Size(max = 200) String positionTitle
) {
}
