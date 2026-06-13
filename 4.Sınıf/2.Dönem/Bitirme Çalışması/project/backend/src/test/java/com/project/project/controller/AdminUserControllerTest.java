package com.project.project.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.dto.admin.AdminUserSummaryResponse;
import com.project.project.dto.user.UserProfileResponse;
import com.project.project.security.JwtAuthenticationFilter;
import com.project.project.service.admin.AdminUserService;

/**
 * Verifies admin profile endpoints for user lookup.
 */
@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listUsersReturnsPagePayload() throws Exception {
        Page<AdminUserSummaryResponse> page = new PageImpl<>(
                List.of(new AdminUserSummaryResponse(11L, "admin@example.com", "ADMIN", true, "#2563EB")),
                PageRequest.of(0, 20),
                1
        );
        when(adminUserService.listUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11))
                .andExpect(jsonPath("$.content[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].enabled").value(true))
                .andExpect(jsonPath("$.content[0].preferredColor").value("#2563EB"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listUsersAcceptsOneBasedPageAndClampsSize() throws Exception {
        Page<AdminUserSummaryResponse> page = new PageImpl<>(
                List.of(new AdminUserSummaryResponse(21L, "user@example.com", "USER", true, "#0891B2")),
                PageRequest.of(0, 100),
                1
        );
        when(adminUserService.listUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users?page=1&size=500")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminUserService).listUsers(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(0, pageable.getPageNumber());
        org.junit.jupiter.api.Assertions.assertEquals(100, pageable.getPageSize());
    }

    @Test
    void listUsersValidationErrorReturns400() throws Exception {
        when(adminUserService.listUsers(any(Pageable.class)))
                .thenThrow(new InvalidPaginationException("Invalid sort property: unknown"));

        mockMvc.perform(get("/api/v1/admin/users?sort=unknown,asc")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAGINATION"))
                .andExpect(jsonPath("$.message").value("Invalid sort property: unknown"));
    }

    @Test
    void getUserProfileReturnsProfileDetails() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                8L,
                "user@example.com",
                "Ali",
                "Kaya",
                "5551112233",
                LocalDate.of(1995, 5, 10),
                false,
                null,
                null,
                null
        );
        when(adminUserService.getUserProfile(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/users/5/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.lastName").value("Kaya"));

        verify(adminUserService).getUserProfile(5L);
    }
}
