package com.project.project.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.dto.user.ApplicationResponse;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserApplicationService;

/**
 * Verifies user application endpoints.
 */
@WebMvcTest(UserApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserApplicationService userApplicationService;

    @Test
    void createApplicationReturnsDraftStatus() throws Exception {
        ApplicationResponse response = new ApplicationResponse(
                1L,
                1L,
                "intern-backend-2026",
                UserApplicationStatus.DRAFT,
                "motivation",
                Instant.now(),
                Instant.now()
        );
        when(userApplicationService.createForCurrentUser(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(response);

        String payload = """
                {
                  "positionKey": "intern-backend-2026",
                  "motivationText": "motivation"
                }
                """;

        mockMvc.perform(post("/api/v1/users/me/applications")
                        .contentType("application/json")
                        .content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void submitApplicationReturnsSubmittedStatus() throws Exception {
        ApplicationResponse response = new ApplicationResponse(
                2L,
                1L,
                "intern-backend-2026",
                UserApplicationStatus.SUBMITTED,
                "motivation",
                Instant.now(),
                Instant.now()
        );
        when(userApplicationService.submitApplication(1L, 2L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/me/applications/2/submit")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void submitApplicationMissingCvReturnsConflict() throws Exception {
        when(userApplicationService.submitApplication(1L, 2L))
                .thenThrow(new com.project.project.config.exception.MissingCvException("Missing CV"));

        mockMvc.perform(patch("/api/v1/users/me/applications/2/submit")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MISSING_CV"));
    }

    @Test
    void submitApplicationMissingAvailabilityReturnsConflict() throws Exception {
        when(userApplicationService.submitApplication(1L, 2L))
                .thenThrow(new com.project.project.config.exception.MissingAvailabilityException("Missing availability"));

        mockMvc.perform(patch("/api/v1/users/me/applications/2/submit")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MISSING_AVAILABILITY"));
    }

    @Test
    void getOtherUsersApplicationReturnsForbidden() throws Exception {
        when(userApplicationService.getForCurrentUser(1L, 99L))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/api/v1/users/me/applications/99")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setEmail("user@example.com");
        account.setRole(Role.USER);
        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
