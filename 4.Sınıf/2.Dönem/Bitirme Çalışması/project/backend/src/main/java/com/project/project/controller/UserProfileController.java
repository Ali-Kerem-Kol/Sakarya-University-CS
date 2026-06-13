package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.user.UserProfileResponse;
import com.project.project.dto.user.UserProfileUpdateRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserProfileService;

/**
 * Exposes profile endpoints for the authenticated user.
 */
@RestController
@RequestMapping("/api/v1/users/me/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(userProfileService.getProfile(principal.getUserAccount().getId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(userProfileService.updateProfile(principal.getUserAccount().getId(), request));
    }
}
