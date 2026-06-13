package com.project.project.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.config.exception.InvalidStatusTransitionException;
import com.project.project.dto.user.ApplicationResponse;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.service.admin.impl.AdminApplicationExportService;
import com.project.project.service.user.UserApplicationService;

/**
 * Verifies admin application status updates.
 */
@WebMvcTest(AdminApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserApplicationService userApplicationService;

    @MockBean
    private AdminApplicationExportService adminApplicationExportService;

    @Test
    void updateStatusInvalidTransitionReturnsConflict() throws Exception {
        when(userApplicationService.adminUpdateStatus(
                org.mockito.ArgumentMatchers.eq(5L),
                org.mockito.ArgumentMatchers.any()
        )).thenThrow(new InvalidStatusTransitionException("Invalid status transition"));

        String payload = """
                {
                  "status": "IN_REVIEW"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/applications/5/status")
                        .contentType("application/json")
                        .content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    void listApplicationsDefaultPageReturnsPagePayload() throws Exception {
        ApplicationResponse response = new ApplicationResponse(
                5L,
                2L,
                "intern-backend-2026",
                UserApplicationStatus.SUBMITTED,
                "motivation",
                Instant.now(),
                Instant.now()
        );
        Page<ApplicationResponse> page = new PageImpl<>(
                java.util.List.of(response),
                PageRequest.of(0, 20),
                1
        );
        when(userApplicationService.adminList(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/applications")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listApplicationsSizeTooLargeReturnsInvalidPagination() throws Exception {
        when(userApplicationService.adminList(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenThrow(new InvalidPaginationException("Page size must be <= 100"));

        mockMvc.perform(get("/api/v1/admin/applications?size=101")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAGINATION"));
    }

    @Test
    void exportApplicationsReturnsCsvContentType() throws Exception {
        org.mockito.Mockito.doNothing().when(adminApplicationExportService).writeApplicationsCsv(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
        mockMvc.perform(get("/api/v1/admin/applications/export")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType("text/csv"));
    }
}
