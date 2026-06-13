package com.project.project.dto.user;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/**
 * Carries availability slot input data from the user.
 *
 * @deprecated use CreateAvailabilitySlotRequest instead.
 */
@Deprecated
public record UserAvailabilitySlotRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {
}
