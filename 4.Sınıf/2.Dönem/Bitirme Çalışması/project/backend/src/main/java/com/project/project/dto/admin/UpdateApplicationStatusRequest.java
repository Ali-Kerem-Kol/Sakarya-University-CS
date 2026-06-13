package com.project.project.dto.admin;

import jakarta.validation.constraints.NotNull;

import com.project.project.entity.UserApplicationStatus;

/**
 * Carries input data for updating application status.
 */
public record UpdateApplicationStatusRequest(
        @NotNull UserApplicationStatus status
) {
}
