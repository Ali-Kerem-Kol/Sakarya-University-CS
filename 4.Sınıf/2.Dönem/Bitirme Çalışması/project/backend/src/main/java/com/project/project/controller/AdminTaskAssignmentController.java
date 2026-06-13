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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import com.project.project.dto.task.TaskAssignmentDetailResponse;
import com.project.project.dto.task.TaskAssignmentListItemResponse;
import com.project.project.dto.task.AdminTaskMutationResponse;
import com.project.project.dto.task.AdminTaskPatchRequest;
import com.project.project.dto.task.AdminTaskReviewRequest;
import com.project.project.dto.task.TaskCreateRequest;
import com.project.project.dto.task.TaskCreateResponse;
import com.project.project.dto.task.TaskFileResponse;
import com.project.project.dto.task.TaskReviewRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.task.TaskAssignmentWorkflowService;

/**
 * Admin task + assignment + review endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminTaskAssignmentController {

    private final TaskAssignmentWorkflowService workflowService;

    public AdminTaskAssignmentController(TaskAssignmentWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskCreateResponse> createTask(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long projectId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        return ResponseEntity.status(201).body(
                workflowService.createTask(principal.getUserAccount().getId(), projectId, request)
        );
    }

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<List<TaskFileResponse>> uploadTaskAttachments(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long taskId,
            @RequestPart("files") MultipartFile[] files
    ) {
        return ResponseEntity.status(201).body(
                workflowService.uploadTaskAttachments(principal.getUserAccount().getId(), taskId, files)
        );
    }

    @GetMapping("/projects/{projectId}/task-assignments")
    public ResponseEntity<List<TaskAssignmentListItemResponse>> listProjectAssignments(@PathVariable Long projectId) {
        return ResponseEntity.ok(workflowService.listAssignmentsByProject(projectId));
    }

    @GetMapping("/users/{userId}/task-assignments")
    public ResponseEntity<List<TaskAssignmentListItemResponse>> listUserAssignments(@PathVariable Long userId) {
        return ResponseEntity.ok(workflowService.listAssignmentsByUserForAdmin(userId));
    }

    @GetMapping("/task-assignments/{assignmentId}")
    public ResponseEntity<TaskAssignmentDetailResponse> getAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(workflowService.getAssignmentForAdmin(assignmentId));
    }

    @PostMapping("/task-assignments/{assignmentId}/review")
    public ResponseEntity<TaskAssignmentDetailResponse> reviewAssignment(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long assignmentId,
            @Valid @RequestBody TaskReviewRequest request
    ) {
        return ResponseEntity.ok(workflowService.reviewAssignment(
                principal.getUserAccount().getId(),
                assignmentId,
                request
        ));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<AdminTaskMutationResponse> patchTask(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody AdminTaskPatchRequest request
    ) {
        return ResponseEntity.ok(workflowService.patchTask(
                principal.getUserAccount().getId(),
                taskId,
                request
        ));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        workflowService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{taskId}/review")
    public ResponseEntity<AdminTaskMutationResponse> reviewTask(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody AdminTaskReviewRequest request
    ) {
        return ResponseEntity.ok(workflowService.reviewTask(
                principal.getUserAccount().getId(),
                taskId,
                request
        ));
    }

    @GetMapping("/tasks/{taskId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadTaskAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId
    ) {
        TaskAssignmentWorkflowService.FileDownload download =
                workflowService.downloadTaskAttachmentForAdmin(taskId, attachmentId);
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

    @GetMapping("/task-assignments/{assignmentId}/submission-files/{fileId}/download")
    public ResponseEntity<Resource> downloadSubmissionFile(
            @PathVariable Long assignmentId,
            @PathVariable Long fileId
    ) {
        TaskAssignmentWorkflowService.FileDownload download =
                workflowService.downloadSubmissionFileForAdmin(assignmentId, fileId);
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
