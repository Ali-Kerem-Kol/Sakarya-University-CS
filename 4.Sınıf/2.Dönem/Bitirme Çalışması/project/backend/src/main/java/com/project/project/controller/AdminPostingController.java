package com.project.project.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

import com.project.project.dto.posting.AdminPostingUpsertRequest;
import com.project.project.dto.posting.AttachmentSummaryResponse;
import com.project.project.dto.posting.PostingListResponse;
import com.project.project.dto.posting.PostingResponse;
import com.project.project.dto.submission.AdminSubmissionListItemResponse;
import com.project.project.dto.submission.AdminSubmissionDetailResponse;
import com.project.project.dto.submission.AdminManualSubmissionRequest;
import com.project.project.dto.submission.SubmissionResponse;
import com.project.project.dto.submission.SubmissionListResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.posting.AdminPostingService;
import com.project.project.service.posting.PostingAttachmentService;
import com.project.project.service.submission.ApplicationSubmissionService;

/**
 * Admin endpoints for postings, attachments and submissions.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminPostingController {
    private static final Logger log = LoggerFactory.getLogger(AdminPostingController.class);

    private final AdminPostingService adminPostingService;
    private final PostingAttachmentService postingAttachmentService;
    private final ApplicationSubmissionService submissionService;

    public AdminPostingController(
            AdminPostingService adminPostingService,
            PostingAttachmentService postingAttachmentService,
            ApplicationSubmissionService submissionService
    ) {
        this.adminPostingService = adminPostingService;
        this.postingAttachmentService = postingAttachmentService;
        this.submissionService = submissionService;
    }

    @PostMapping("/postings")
    public ResponseEntity<PostingResponse> createPosting(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody AdminPostingUpsertRequest request
    ) {
        return ResponseEntity.status(201)
                .body(adminPostingService.createDraft(principal.getUserAccount().getId(), request));
    }

    @PutMapping("/postings/{id}")
    public ResponseEntity<PostingResponse> updatePosting(
            @PathVariable Long id,
            @Valid @RequestBody AdminPostingUpsertRequest request
    ) {
        return ResponseEntity.ok(adminPostingService.update(id, request));
    }

    @PostMapping("/postings/{id}/publish")
    public ResponseEntity<PostingResponse> publishPosting(@PathVariable Long id) {
        return ResponseEntity.ok(adminPostingService.publish(id));
    }

    @PostMapping("/postings/{id}/close")
    public ResponseEntity<PostingResponse> closePosting(@PathVariable Long id) {
        return ResponseEntity.ok(adminPostingService.close(id));
    }

    @GetMapping("/postings")
    public ResponseEntity<PostingListResponse> listPostings(
            @RequestParam(required = false) ApplicationCategory category,
            @RequestParam(required = false) ApplicationPostingStatus status
    ) {
        return ResponseEntity.ok(adminPostingService.list(category, status));
    }

    @PostMapping("/postings/{id}/attachments")
    public ResponseEntity<List<AttachmentSummaryResponse>> uploadAttachments(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files
    ) {
        log.info(
                "Posting attachment upload request: postingId={}, uploaderId={}, fileCount={}",
                id,
                principal.getUserAccount().getId(),
                files != null ? files.size() : 0
        );
        return ResponseEntity.status(201)
                .body(postingAttachmentService.upload(principal.getUserAccount().getId(), id, files));
    }

    @DeleteMapping("/postings/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId
    ) {
        postingAttachmentService.delete(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/postings/{id}/submissions")
    public ResponseEntity<SubmissionListResponse> listPostingSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.listByPosting(id));
    }

    @PostMapping("/postings/{id}/submissions/manual")
    public ResponseEntity<SubmissionResponse> addSubmissionManually(
            @PathVariable Long id,
            @Valid @RequestBody AdminManualSubmissionRequest request
    ) {
        return ResponseEntity.status(201).body(
                submissionService.addOrUpdateByAdmin(id, request.userId(), request.resolvedStatus())
        );
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<AdminSubmissionDetailResponse> getSubmissionDetail(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.adminDetail(id));
    }

    @PostMapping("/submissions/{id}/accept")
    public ResponseEntity<SubmissionResponse> acceptSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.accept(id));
    }

    @PostMapping("/submissions/{id}/approve")
    public ResponseEntity<SubmissionResponse> approveSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.accept(id));
    }

    @PostMapping("/submissions/{id}/reject")
    public ResponseEntity<SubmissionResponse> rejectSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.reject(id));
    }

    @PostMapping("/submissions/{id}/remove")
    public ResponseEntity<SubmissionResponse> removeSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.remove(id));
    }

    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<SubmissionResponse> deleteSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.deleteByAdmin(id));
    }

    @GetMapping("/submissions")
    public ResponseEntity<Page<AdminSubmissionListItemResponse>> listSubmissions(
            @RequestParam(required = false) ApplicationCategory category,
            @RequestParam(required = false) ApplicationSubmissionStatus status,
            @RequestParam(required = false) ApplicationPostingStatus postingStatus,
            @RequestParam(required = false) Long postingId,
            @PageableDefault(sort = "submittedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(submissionService.listForAdmin(category, status, postingStatus, postingId, pageable));
    }

    @PostMapping("/postings/{id}/reopen")
    public ResponseEntity<PostingResponse> reopenPosting(@PathVariable Long id) {
        return ResponseEntity.ok(adminPostingService.reopen(id));
    }
}
