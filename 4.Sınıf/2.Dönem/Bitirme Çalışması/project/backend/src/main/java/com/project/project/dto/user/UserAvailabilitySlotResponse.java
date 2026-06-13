package com.project.project.dto.user;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Exposes stored availability slot information.
 */
public record UserAvailabilitySlotResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
