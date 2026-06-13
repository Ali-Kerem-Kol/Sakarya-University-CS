package com.project.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.task.TaskAssignmentUpdateRequest;
import com.project.project.dto.task.TaskResponse;
import com.project.project.dto.task.TaskUpsertRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.task.TaskService;

/**
 * Admin task management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminTaskController {

    private final TaskService taskService;

    public AdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/postings/{postingId}/tasks")
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long postingId,
            @Valid @RequestBody TaskUpsertRequest request
    ) {
        return ResponseEntity.status(201).body(taskService.create(principal.getUserAccount().getId(), postingId, request));
    }

    @GetMapping("/postings/{postingId}/tasks")
    public ResponseEntity<List<TaskResponse>> listByPosting(@PathVariable Long postingId) {
        return ResponseEntity.ok(taskService.listByPosting(postingId));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpsertRequest request
    ) {
        return ResponseEntity.ok(taskService.update(taskId, request));
    }

    @DeleteMapping("/legacy/tasks/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId) {
        taskService.delete(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{taskId}/assign/{userId}")
    public ResponseEntity<TaskResponse> assign(
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @RequestBody(required = false) TaskAssignmentUpdateRequest request
    ) {
        return ResponseEntity.ok(taskService.assign(taskId, userId, request));
    }

    @PostMapping("/tasks/{taskId}/mark-done/{userId}")
    public ResponseEntity<TaskResponse> markDone(
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @RequestBody(required = false) TaskAssignmentUpdateRequest request
    ) {
        return ResponseEntity.ok(taskService.markDone(taskId, userId, request));
    }
}
