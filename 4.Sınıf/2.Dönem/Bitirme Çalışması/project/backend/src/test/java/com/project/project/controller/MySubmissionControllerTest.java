package com.project.project.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.ConflictException;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.submission.ApplicationSubmissionService;

@WebMvcTest(MySubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MySubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationSubmissionService applicationSubmissionService;

    @Test
    void duplicateSubmissionReturnsConflict() throws Exception {
        when(applicationSubmissionService.create(eq(1L), eq(42L)))
                .thenThrow(new ConflictException("ALREADY_SUBMITTED"));

        mockMvc.perform(post("/api/v1/me/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postingId\":42}")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationUser())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    private UsernamePasswordAuthenticationToken authenticationUser() {
        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setEmail("user@ogr.sakarya.edu.tr");
        account.setRole(Role.USER);
        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
