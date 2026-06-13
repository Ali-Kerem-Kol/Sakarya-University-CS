package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.task.TaskGraphResponse;
import com.project.project.service.task.TaskGraphService;

/**
 * Admin graph endpoint for project task flow.
 */
@RestController
@RequestMapping("/api/v1/admin/projects")
public class AdminTaskGraphController {

    private final TaskGraphService taskGraphService;

    public AdminTaskGraphController(TaskGraphService taskGraphService) {
        this.taskGraphService = taskGraphService;
    }

    @GetMapping("/{projectId}/task-graph")
    public ResponseEntity<TaskGraphResponse> getProjectTaskGraph(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskGraphService.getAdminProjectGraph(projectId));
    }
}
