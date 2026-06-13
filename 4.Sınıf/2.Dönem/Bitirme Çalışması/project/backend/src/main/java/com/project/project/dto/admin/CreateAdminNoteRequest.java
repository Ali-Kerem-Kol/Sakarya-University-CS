package com.project.project.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Carries input data for creating an admin note.
 */
public record CreateAdminNoteRequest(
        @NotBlank @Size(max = 2000) String noteText
) {
}
