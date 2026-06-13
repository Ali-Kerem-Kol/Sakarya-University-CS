package com.project.project.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.task.TaskTimelineEventResponse;
import com.project.project.service.task.TaskTimelineService;

/**
 * Admin timeline views for project and user task flows.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminTaskTimelineController {

    private final TaskTimelineService taskTimelineService;

    public AdminTaskTimelineController(TaskTimelineService taskTimelineService) {
        this.taskTimelineService = taskTimelineService;
    }

    @GetMapping("/projects/{projectId}/timeline")
    public ResponseEntity<Page<TaskTimelineEventResponse>> getProjectTimeline(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(taskTimelineService.getProjectTimeline(projectId, page, size));
    }

    @GetMapping("/users/{userId}/timeline")
    public ResponseEntity<Page<TaskTimelineEventResponse>> getUserTimeline(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(taskTimelineService.getUserTimeline(userId, page, size));
    }
}
