package com.project.project.service.user.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

import com.project.project.config.exception.AvailabilityOverlapException;
import com.project.project.config.exception.InvalidAvailabilityException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.user.AvailabilityListResponse;
import com.project.project.dto.user.AvailabilitySlotResponse;
import com.project.project.dto.user.CreateAvailabilitySlotRequest;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserAvailabilitySlot;
import com.project.project.entity.UserProfile;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserAvailabilitySlotRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.user.UserAvailabilityService;

/**
 * Implements availability management operations for the authenticated user.
 */
@Service
public class UserAvailabilityServiceImpl implements UserAvailabilityService {

    private static final String DEFAULT_TIMEZONE = "Europe/Istanbul";
    private static final Duration MIN_SLOT_DURATION = Duration.ofMinutes(30);

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserAvailabilitySlotRepository userAvailabilitySlotRepository;

    public UserAvailabilityServiceImpl(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            UserAvailabilitySlotRepository userAvailabilitySlotRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.userAvailabilitySlotRepository = userAvailabilitySlotRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityListResponse getAvailability(Long userId) {
        return buildResponse(userId);
    }

    @Override
    @Transactional
    public AvailabilitySlotResponse createSlot(Long userId, CreateAvailabilitySlotRequest request) {
        UserProfile profile = ensureProfile(userId);
        validateSlot(request);
        boolean overlaps = userAvailabilitySlotRepository.existsOverlappingSlot(
                userId,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );
        if (overlaps) {
            throw new AvailabilityOverlapException("Availability overlaps with existing slot");
        }
        UserAvailabilitySlot slot = new UserAvailabilitySlot();
        slot.setProfile(profile);
        slot.setDayOfWeek(request.dayOfWeek());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setTimezone(DEFAULT_TIMEZONE);
        UserAvailabilitySlot saved = userAvailabilitySlotRepository.save(slot);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        UserAvailabilitySlot slot = userAvailabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Availability slot not found"));
        if (!slot.getProfile().getUserAccount().getId().equals(userId)) {
            throw new AccessDeniedException("Forbidden");
        }
        userAvailabilitySlotRepository.delete(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityListResponse getAvailabilityForUser(Long userId) {
        UserAccount account = getUserAccount(userId);
        if (account.getProfile() == null) {
            return new AvailabilityListResponse(List.of());
        }
        return buildResponse(userId);
    }

    private UserProfile ensureProfile(Long userId) {
        UserAccount userAccount = getUserAccount(userId);
        UserProfile profile = userAccount.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserAccount(userAccount);
            profile = userProfileRepository.save(profile);
        }
        return profile;
    }

    private AvailabilityListResponse buildResponse(Long userId) {
        List<UserAvailabilitySlot> slots = new ArrayList<>(
                userAvailabilitySlotRepository.findByProfileUserAccountId(userId)
        );
        slots.sort(Comparator
                .comparing((UserAvailabilitySlot slot) -> slot.getDayOfWeek().getValue())
                .thenComparing(UserAvailabilitySlot::getStartTime)
        );
        List<AvailabilitySlotResponse> responses = new ArrayList<>();
        for (UserAvailabilitySlot slot : slots) {
            responses.add(toResponse(slot));
        }
        return new AvailabilityListResponse(responses);
    }

    private AvailabilitySlotResponse toResponse(UserAvailabilitySlot slot) {
        return new AvailabilitySlotResponse(
                slot.getId(),
                slot.getDayOfWeek(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getTimezone(),
                slot.getCreatedAt()
        );
    }

    private void validateSlot(CreateAvailabilitySlotRequest request) {
        if (request == null || request.dayOfWeek() == null || request.startTime() == null || request.endTime() == null) {
            throw new InvalidAvailabilityException("Availability requires day and time values");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new InvalidAvailabilityException("Start time must be before end time");
        }
        Duration duration = Duration.between(request.startTime(), request.endTime());
        if (duration.compareTo(MIN_SLOT_DURATION) < 0) {
            throw new InvalidAvailabilityException("Availability slot must be at least 30 minutes");
        }
    }

    private UserAccount getUserAccount(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
