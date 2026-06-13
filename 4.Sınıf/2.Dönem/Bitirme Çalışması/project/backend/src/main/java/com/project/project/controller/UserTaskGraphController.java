package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.task.TaskGraphResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.task.TaskGraphService;

/**
 * Student graph endpoint for own branch + main branch.
 */
@RestController
@RequestMapping("/api/v1/users/me")
public class UserTaskGraphController {

    private final TaskGraphService taskGraphService;

    public UserTaskGraphController(TaskGraphService taskGraphService) {
        this.taskGraphService = taskGraphService;
    }

    @GetMapping("/task-graph")
    public ResponseEntity<TaskGraphResponse> getMyTaskGraph(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(
                taskGraphService.getStudentProjectGraph(principal.getUserAccount().getId(), projectId)
        );
    }
}
