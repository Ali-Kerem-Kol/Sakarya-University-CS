package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.submission.CreateSubmissionRequest;
import com.project.project.dto.submission.SubmissionListResponse;
import com.project.project.dto.submission.SubmissionResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.submission.ApplicationSubmissionService;

/**
 * Endpoints for current user's posting submissions.
 */
@RestController
@RequestMapping("/api/v1/me/submissions")
public class MySubmissionController {

    private final ApplicationSubmissionService submissionService;

    public MySubmissionController(ApplicationSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> create(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        return ResponseEntity.status(201)
                .body(submissionService.create(principal.getUserAccount().getId(), request.postingId()));
    }

    @GetMapping
    public ResponseEntity<SubmissionListResponse> listMine(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(submissionService.listMine(principal.getUserAccount().getId()));
    }
}
