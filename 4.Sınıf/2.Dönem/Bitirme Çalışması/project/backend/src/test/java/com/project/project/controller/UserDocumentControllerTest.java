package com.project.project.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.io.ByteArrayResource;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.InvalidFileTypeException;
import com.project.project.dto.user.DocumentResponse;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.security.JwtService;
import com.project.project.security.UserAccountDetailsService;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserDocumentService;

/**
 * Verifies document upload and access rules for user endpoints.
 */
@WebMvcTest(UserDocumentController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(GlobalExceptionHandler.class)
class UserDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDocumentService userDocumentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserAccountDetailsService userAccountDetailsService;

    @Test
    void uploadCvSuccessReturnsDocumentResponse() throws Exception {
        DocumentResponse response = new DocumentResponse(
                1L,
                "CV",
                "cv.pdf",
                "application/pdf",
                1024L,
                Instant.now()
        );
        when(userDocumentService.uploadCv(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/documents/cv")
                        .file(file)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CV"))
                .andExpect(jsonPath("$.originalFileName").value("cv.pdf"));
    }

    @Test
    void uploadCvInvalidTypeReturnsBadRequest() throws Exception {
        when(userDocumentService.uploadCv(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new InvalidFileTypeException("CV must be a PDF"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.txt",
                "text/plain",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/me/documents/cv")
                        .file(file)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_TYPE"))
                .andExpect(jsonPath("$.code").value("INVALID_FILE_TYPE"))
                .andExpect(jsonPath("$.message").value("Only PDF is allowed"));
    }

    @Test
    void downloadOtherUsersDocumentReturnsForbidden() throws Exception {
        when(userDocumentService.downloadDocument(1L, 99L))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/api/v1/users/me/documents/99/download")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void downloadDocumentAlwaysReturnsInlinePdfHeaders() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("pdf-content".getBytes());
        when(userDocumentService.downloadDocument(1L, 42L))
                .thenReturn(new UserDocumentService.DocumentDownload(resource, "text/plain", "cv.pdf"));

        mockMvc.perform(get("/api/v1/users/me/documents/42/download")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("inline;")));
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
