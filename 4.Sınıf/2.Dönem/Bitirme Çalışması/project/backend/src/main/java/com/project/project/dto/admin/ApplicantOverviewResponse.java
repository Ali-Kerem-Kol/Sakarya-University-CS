package com.project.project.dto.admin;

import java.time.Instant;

import com.project.project.entity.UserApplicationStatus;

/**
 * Exposes applicant overview information for admin dashboards.
 */
public record ApplicantOverviewResponse(
        Long userId,
        String email,
        String firstName,
        String lastName,
        Long latestApplicationId,
        String latestPositionKey,
        UserApplicationStatus latestStatus,
        Instant latestLastStatusChangedAt,
        boolean hasCv,
        int availabilitySlotCount
) {
}
