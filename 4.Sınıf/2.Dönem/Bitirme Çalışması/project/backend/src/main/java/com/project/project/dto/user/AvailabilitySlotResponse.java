package com.project.project.dto.user;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

/**
 * Exposes availability slot details for responses.
 */
public record AvailabilitySlotResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String timezone,
        Instant createdAt
) {
}
