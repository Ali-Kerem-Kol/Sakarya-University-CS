package com.project.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.task.TaskResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.task.TaskService;

/**
 * Current user's task list endpoint.
 */
@RestController
@RequestMapping("/api/v1/me/tasks")
public class MyTaskController {

    private final TaskService taskService;

    public MyTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listMine(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestParam(required = false) Long postingId
    ) {
        return ResponseEntity.ok(taskService.listMine(principal.getUserAccount().getId(), postingId));
    }
}
