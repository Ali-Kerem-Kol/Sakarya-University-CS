package com.project.project.dto.posting;

import com.project.project.entity.ApplicationCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating/updating postings.
 */
public record AdminPostingUpsertRequest(
        @NotNull ApplicationCategory category,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotBlank @Size(max = 200) String projectName,
        @NotBlank @Size(max = 5000) String projectDetails
) {
}
