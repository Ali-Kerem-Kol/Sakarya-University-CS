package com.project.project.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.dto.task.TaskAssignmentDetailResponse;
import com.project.project.dto.task.TaskAssignmentListItemResponse;
import com.project.project.dto.task.TaskSubmissionDataRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.task.TaskAssignmentWorkflowService;

import jakarta.validation.Valid;

/**
 * Student task assignment endpoints.
 */
@RestController
@RequestMapping("/api/v1/me/task-assignments")
public class MyTaskAssignmentController {

    private final TaskAssignmentWorkflowService workflowService;

    public MyTaskAssignmentController(TaskAssignmentWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ResponseEntity<List<TaskAssignmentListItemResponse>> listMine(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(workflowService.listMyAssignments(principal.getUserAccount().getId()));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<TaskAssignmentDetailResponse> getMine(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(
                workflowService.getMyAssignment(principal.getUserAccount().getId(), assignmentId)
        );
    }

    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<TaskAssignmentDetailResponse> submit(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long assignmentId,
            @Valid @RequestPart("data") TaskSubmissionDataRequest data,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        return ResponseEntity.ok(
                workflowService.submitAssignment(principal.getUserAccount().getId(), assignmentId, data, files)
        );
    }

    @GetMapping("/{assignmentId}/task-attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadTaskAttachment(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long assignmentId,
            @PathVariable Long attachmentId
    ) {
        TaskAssignmentWorkflowService.FileDownload download = workflowService.downloadTaskAttachmentForStudent(
                principal.getUserAccount().getId(),
                assignmentId,
                attachmentId
        );
        String fileName = (download.originalFileName() == null || download.originalFileName().isBlank())
                ? "task-attachment.pdf"
                : download.originalFileName();
        String disposition = ContentDisposition.inline()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(download.resource());
    }

    @GetMapping("/{assignmentId}/submission-files/{fileId}/download")
    public ResponseEntity<Resource> downloadSubmissionFile(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long assignmentId,
            @PathVariable Long fileId
    ) {
        TaskAssignmentWorkflowService.FileDownload download = workflowService.downloadSubmissionFileForStudent(
                principal.getUserAccount().getId(),
                assignmentId,
                fileId
        );
        String fileName = (download.originalFileName() == null || download.originalFileName().isBlank())
                ? "task-submission.pdf"
                : download.originalFileName();
        String disposition = ContentDisposition.inline()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(download.resource());
    }
}
