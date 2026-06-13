package com.project.project.dto.user;

import java.util.List;

/**
 * Wraps availability slots for list responses.
 */
public record AvailabilityListResponse(List<AvailabilitySlotResponse> slots) {
}
