package com.project.project.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.admin.AdminNoteResponse;
import com.project.project.service.admin.AdminNoteService;

/**
 * Verifies admin note endpoints for application notes.
 */
@WebMvcTest(AdminNoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminNoteService adminNoteService;

    @Test
    void createNoteReturnsResponse() throws Exception {
        AdminNoteResponse response = new AdminNoteResponse(
                1L,
                10L,
                "note",
                Instant.now(),
                "admin@example.com"
        );
        when(adminNoteService.addNote(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq("admin"),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(response);

        String payload = """
                {
                  "noteText": "note"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/applications/10/notes")
                        .contentType("application/json")
                        .content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.noteText").value("note"))
                .andExpect(jsonPath("$.applicationId").value(10L));
    }

    @Test
    void createNoteApplicationNotFoundReturns404() throws Exception {
        when(adminNoteService.addNote(
                org.mockito.ArgumentMatchers.eq(99L),
                org.mockito.ArgumentMatchers.eq("admin"),
                org.mockito.ArgumentMatchers.any()
        )).thenThrow(new NotFoundException("Application not found"));

        String payload = """
                {
                  "noteText": "note"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/applications/99/notes")
                        .contentType("application/json")
                        .content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }
}
