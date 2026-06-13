package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.user.ApplicationListResponse;
import com.project.project.dto.user.ApplicationResponse;
import com.project.project.dto.user.CreateApplicationRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserApplicationService;

/**
 * Exposes endpoints for user application creation and listing.
 */
@RestController
@RequestMapping("/api/v1/users/me/applications")
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    public UserApplicationController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.ok(
                userApplicationService.createForCurrentUser(principal.getUserAccount().getId(), request)
        );
    }

    @GetMapping
    public ResponseEntity<ApplicationListResponse> listMyApplications(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(
                userApplicationService.listForCurrentUser(principal.getUserAccount().getId())
        );
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getMyApplication(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                userApplicationService.getForCurrentUser(principal.getUserAccount().getId(), applicationId)
        );
    }

    @PatchMapping("/{applicationId}/submit")
    public ResponseEntity<ApplicationResponse> submitApplication(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                userApplicationService.submitApplication(principal.getUserAccount().getId(), applicationId)
        );
    }
}
