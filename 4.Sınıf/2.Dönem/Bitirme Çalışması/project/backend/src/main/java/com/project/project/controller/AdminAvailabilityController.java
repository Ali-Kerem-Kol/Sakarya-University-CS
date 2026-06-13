package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.user.AvailabilityListResponse;
import com.project.project.service.user.UserAvailabilityService;

/**
 * Exposes admin endpoints for viewing user availability.
 */
@RestController
@RequestMapping("/api/v1/admin/users/{userId}/availability")
public class AdminAvailabilityController {

    private final UserAvailabilityService userAvailabilityService;

    public AdminAvailabilityController(UserAvailabilityService userAvailabilityService) {
        this.userAvailabilityService = userAvailabilityService;
    }

    @GetMapping
    public ResponseEntity<AvailabilityListResponse> getAvailability(@PathVariable Long userId) {
        return ResponseEntity.ok(userAvailabilityService.getAvailabilityForUser(userId));
    }
}
