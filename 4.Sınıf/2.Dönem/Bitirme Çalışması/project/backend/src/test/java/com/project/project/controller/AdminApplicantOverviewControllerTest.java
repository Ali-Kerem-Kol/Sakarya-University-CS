package com.project.project.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.dto.admin.ApplicantOverviewResponse;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.service.admin.AdminApplicantOverviewService;
import com.project.project.service.admin.impl.AdminApplicantOverviewExportService;

/**
 * Verifies admin applicant overview endpoint behavior.
 */
@WebMvcTest(AdminApplicantOverviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminApplicantOverviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminApplicantOverviewService overviewService;

    @MockBean
    private AdminApplicantOverviewExportService overviewExportService;

    @Test
    void defaultCallReturnsPagePayload() throws Exception {
        ApplicantOverviewResponse response = new ApplicantOverviewResponse(
                1L,
                "user@example.com",
                "Ali",
                "Kaya",
                10L,
                "intern-backend-2026",
                UserApplicationStatus.SUBMITTED,
                Instant.now(),
                true,
                2
        );
        Page<ApplicantOverviewResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 20),
                1
        );
        when(overviewService.getOverview(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/overview/applicants")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void sizeTooLargeReturnsInvalidPagination() throws Exception {
        when(overviewService.getOverview(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenThrow(new InvalidPaginationException("Page size must be between 1 and 100"));

        mockMvc.perform(get("/api/v1/admin/overview/applicants?size=101")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAGINATION"));
    }

    @Test
    void queryFiltersArePassedToService() throws Exception {
        Page<ApplicantOverviewResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );
        when(overviewService.getOverview(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("ali"),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/overview/applicants?q=ali")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(overviewService).getOverview(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("ali"),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
