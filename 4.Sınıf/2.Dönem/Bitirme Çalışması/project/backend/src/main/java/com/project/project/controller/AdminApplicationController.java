package com.project.project.controller;

import java.io.Writer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.admin.UpdateApplicationStatusRequest;
import com.project.project.dto.user.ApplicationResponse;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.service.admin.impl.AdminApplicationExportService;
import com.project.project.service.user.UserApplicationService;

/**
 * Exposes admin endpoints for managing user applications.
 */
@RestController
@RequestMapping("/api/v1/admin/applications")
public class AdminApplicationController {

    private final UserApplicationService userApplicationService;
    private final AdminApplicationExportService adminApplicationExportService;

    public AdminApplicationController(
            UserApplicationService userApplicationService,
            AdminApplicationExportService adminApplicationExportService
    ) {
        this.userApplicationService = userApplicationService;
        this.adminApplicationExportService = adminApplicationExportService;
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> listApplications(
            @RequestParam(required = false) UserApplicationStatus status,
            @RequestParam(required = false) String positionKey,
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(userApplicationService.adminList(status, positionKey, pageable));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(userApplicationService.adminGetById(applicationId));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        return ResponseEntity.ok(
                userApplicationService.adminUpdateStatus(applicationId, request.status())
        );
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportApplications(
            @RequestParam(required = false) UserApplicationStatus status,
            @RequestParam(required = false) String positionKey,
            @RequestParam(required = false, name = "q") String query,
            @org.springframework.data.web.SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Sort sort
    ) {
        StreamingResponseBody body = outputStream -> {
            try (Writer writer = adminApplicationExportService.buildWriter(outputStream)) {
                adminApplicationExportService.writeApplicationsCsv(status, positionKey, query, sort, writer);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/csv"))
                .body(body);
    }
}
