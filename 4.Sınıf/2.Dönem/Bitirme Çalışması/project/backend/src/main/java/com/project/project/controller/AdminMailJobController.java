package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import com.project.project.dto.mail.MailJobCreateRequest;
import com.project.project.dto.mail.MailJobResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.mail.MailJobService;

/**
 * Admin endpoints for mail job creation and status.
 */
@RestController
@RequestMapping("/api/v1/admin/mail/jobs")
public class AdminMailJobController {

    private final MailJobService mailJobService;

    public AdminMailJobController(MailJobService mailJobService) {
        this.mailJobService = mailJobService;
    }

    @PostMapping(value = "/category/{category}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MailJobResponse> createCategoryJob(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable ApplicationCategory category,
            @Valid @RequestPart("data") MailJobCreateRequest request,
            @RequestPart(value = "files", required = false) java.util.List<MultipartFile> files
    ) {
        return ResponseEntity.status(201)
                .body(mailJobService.createCategoryJob(principal.getUserAccount().getId(), category, request, files));
    }

    @PostMapping(value = "/posting/{postingId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MailJobResponse> createPostingJob(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long postingId,
            @Valid @RequestPart("data") MailJobCreateRequest request,
            @RequestPart(value = "files", required = false) java.util.List<MultipartFile> files
    ) {
        return ResponseEntity.status(201)
                .body(mailJobService.createPostingJob(principal.getUserAccount().getId(), postingId, request, files));
    }

    @PostMapping(value = "/all-students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MailJobResponse> createAllStudentsJob(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestPart("data") MailJobCreateRequest request,
            @RequestPart(value = "files", required = false) java.util.List<MultipartFile> files
    ) {
        return ResponseEntity.status(201)
                .body(mailJobService.createAllStudentsJob(principal.getUserAccount().getId(), request, files));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<MailJobResponse> get(@PathVariable Long jobId) {
        return ResponseEntity.ok(mailJobService.get(jobId));
    }
}
