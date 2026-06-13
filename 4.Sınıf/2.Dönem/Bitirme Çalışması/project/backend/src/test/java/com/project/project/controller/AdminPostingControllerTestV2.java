package com.project.project.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
import com.project.project.dto.posting.PostingResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.posting.AdminPostingService;
import com.project.project.service.posting.PostingAttachmentService;
import com.project.project.service.submission.ApplicationSubmissionService;

@WebMvcTest(AdminPostingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminPostingControllerTestV2 {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminPostingService adminPostingService;

    @MockBean
    private PostingAttachmentService postingAttachmentService;

    @MockBean
    private ApplicationSubmissionService applicationSubmissionService;

    @Test
    void publishTransitionReturnsPublished() throws Exception {
        PostingResponse response = new PostingResponse(
                10L,
                ApplicationCategory.BACKEND,
                "Backend",
                "Desc",
                "Proj",
                "Details",
                ApplicationPostingStatus.PUBLISHED,
                Instant.now(),
                null,
                Instant.now(),
                List.of()
        );
        when(adminPostingService.publish(eq(10L))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/postings/10/publish")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    private UsernamePasswordAuthenticationToken authenticationAdmin() {
        UserAccount account = new UserAccount();
        account.setId(99L);
        account.setEmail("admin@ogr.sakarya.edu.tr");
        account.setRole(Role.ADMIN);
        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
