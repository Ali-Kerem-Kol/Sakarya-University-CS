package com.project.project.service.user;

import com.project.project.dto.user.AvailabilityListResponse;
import com.project.project.dto.user.AvailabilitySlotResponse;
import com.project.project.dto.user.CreateAvailabilitySlotRequest;

/**
 * Defines operations for user availability management.
 */
public interface UserAvailabilityService {

    AvailabilityListResponse getAvailability(Long userId);

    AvailabilitySlotResponse createSlot(Long userId, CreateAvailabilitySlotRequest request);

    void deleteSlot(Long userId, Long slotId);

    AvailabilityListResponse getAvailabilityForUser(Long userId);
}
