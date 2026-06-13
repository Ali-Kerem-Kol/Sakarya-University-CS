package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.user.AvailabilityListResponse;
import com.project.project.dto.user.AvailabilitySlotResponse;
import com.project.project.dto.user.CreateAvailabilitySlotRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserAvailabilityService;

/**
 * Exposes endpoints for managing the authenticated user's availability.
 */
@RestController
@RequestMapping("/api/v1/users/me/availability")
public class UserAvailabilityController {

    private final UserAvailabilityService userAvailabilityService;

    public UserAvailabilityController(UserAvailabilityService userAvailabilityService) {
        this.userAvailabilityService = userAvailabilityService;
    }

    @GetMapping
    public ResponseEntity<AvailabilityListResponse> getAvailability(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(userAvailabilityService.getAvailability(principal.getUserAccount().getId()));
    }

    @PostMapping
    public ResponseEntity<AvailabilitySlotResponse> createAvailability(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody CreateAvailabilitySlotRequest request
    ) {
        return ResponseEntity.ok(
                userAvailabilityService.createSlot(principal.getUserAccount().getId(), request)
        );
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteAvailability(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long slotId
    ) {
        userAvailabilityService.deleteSlot(principal.getUserAccount().getId(), slotId);
        return ResponseEntity.noContent().build();
    }
}
