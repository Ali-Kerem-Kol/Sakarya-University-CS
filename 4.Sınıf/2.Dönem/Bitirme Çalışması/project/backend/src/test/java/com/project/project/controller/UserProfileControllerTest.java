package com.project.project.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.dto.user.UserProfileResponse;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserProfileService;

/**
 * Verifies user profile endpoints with an authenticated principal.
 */
@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Test
    void getProfileReturnsCurrentUserProfile() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                10L,
                "user@example.com",
                "Ali",
                "Kaya",
                "5551112233",
                LocalDate.of(1995, 5, 10),
                true,
                "cv.pdf",
                java.time.Instant.parse("2026-02-20T09:00:00Z"),
                "/users/me/documents/42/download"
        );
        when(userProfileService.getProfile(1L)).thenReturn(response);

        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setEmail("user@example.com");
        account.setRole(Role.USER);

        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        mockMvc.perform(get("/api/v1/users/me/profile")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.hasCv").value(true))
                .andExpect(jsonPath("$.cvFileName").value("cv.pdf"));

        verify(userProfileService).getProfile(1L);
    }
}
