package com.project.project.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.Writer;

import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.project.project.dto.admin.ApplicantOverviewResponse;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.service.admin.AdminApplicantOverviewService;
import com.project.project.service.admin.impl.AdminApplicantOverviewExportService;

/**
 * Exposes admin overview dashboard endpoints for applicants.
 */
@RestController
@RequestMapping("/api/v1/admin/overview/applicants")
public class AdminApplicantOverviewController {

    private final AdminApplicantOverviewService overviewService;
    private final AdminApplicantOverviewExportService overviewExportService;

    public AdminApplicantOverviewController(
            AdminApplicantOverviewService overviewService,
            AdminApplicantOverviewExportService overviewExportService
    ) {
        this.overviewService = overviewService;
        this.overviewExportService = overviewExportService;
    }

    @GetMapping
    public ResponseEntity<Page<ApplicantOverviewResponse>> getOverview(
            @RequestParam(required = false) UserApplicationStatus status,
            @RequestParam(required = false) String positionKey,
            @RequestParam(required = false) Boolean hasCv,
            @RequestParam(required = false) Boolean hasAvailability,
            @RequestParam(required = false, name = "q") String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                overviewService.getOverview(
                        status,
                        positionKey,
                        hasCv,
                        hasAvailability,
                        query,
                        pageable
                )
        );
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportOverview(
            @RequestParam(required = false) UserApplicationStatus status,
            @RequestParam(required = false) String positionKey,
            @RequestParam(required = false) Boolean hasCv,
            @RequestParam(required = false) Boolean hasAvailability,
            @RequestParam(required = false, name = "q") String query,
            @org.springframework.data.web.SortDefault(sort = "latestLastStatusChangedAt",
                    direction = Sort.Direction.DESC)
            Sort sort
    ) {
        StreamingResponseBody body = outputStream -> {
            try (Writer writer = overviewExportService.buildWriter(outputStream)) {
                overviewExportService.writeOverviewCsv(
                        status,
                        positionKey,
                        hasCv,
                        hasAvailability,
                        query,
                        sort,
                        writer
                );
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/csv"))
                .body(body);
    }
}
