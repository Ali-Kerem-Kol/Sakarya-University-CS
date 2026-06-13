package com.project.project.dto.user;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

/**
 * Carries input data for creating a new availability slot.
 */
public record CreateAvailabilitySlotRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime endTime
) {
}
