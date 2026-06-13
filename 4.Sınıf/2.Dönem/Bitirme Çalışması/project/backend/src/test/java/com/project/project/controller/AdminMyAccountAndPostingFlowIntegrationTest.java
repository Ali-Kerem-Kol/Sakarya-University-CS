package com.project.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.UserAccountRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "management.health.mail.enabled=false"
})
class AdminMyAccountAndPostingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanUpdateProfilePasswordEmailUserEmailUpdateForbidden() throws Exception {
        seedAccount("admin.contract@32bit.com.tr", "Admin12345!", Role.ADMIN);
        seedAccount("student.contract@ogr.sakarya.edu.tr", "User12345!", Role.USER);

        String adminToken = loginAndGetToken("admin.contract@32bit.com.tr", "Admin12345!");
        String userToken = loginAndGetToken("student.contract@ogr.sakarya.edu.tr", "User12345!");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student.contract@ogr.sakarya.edu.tr"))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(get("/api/v1/admin/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin.contract@32bit.com.tr"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student.contract@ogr.sakarya.edu.tr"));

        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin.contract@32bit.com.tr"));

        mockMvc.perform(put("/api/v1/me/email")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated.student@ogr.sakarya.edu.tr\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        mockMvc.perform(put("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Admin",
                                  "lastName":"Updated",
                                  "phoneNumber":"5551112233"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Admin"));

        mockMvc.perform(put("/api/v1/me/password")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword":"Admin12345!",
                                  "newPassword":"Admin12345!X"
                                }
                                """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value("Password changed"));

        mockMvc.perform(put("/api/v1/me/email")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin.updated@32bit.com.tr\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin.updated@32bit.com.tr"));

        String reloginToken = loginAndGetToken("admin.updated@32bit.com.tr", "Admin12345!X");
        assertThat(reloginToken).isNotBlank();
    }

    @Test
    void createDraftAppearsInAdminListAndOnlyPublishedInPublicList() throws Exception {
        seedAccount("admin.posting@32bit.com.tr", "Admin12345!", Role.ADMIN);
        String adminToken = loginAndGetToken("admin.posting@32bit.com.tr", "Admin12345!");

        String createBody = """
                {
                  "category":"BACKEND",
                  "title":"Visibility Test Posting",
                  "description":"Desc",
                  "projectName":"ATS",
                  "projectDetails":"Details"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/admin/postings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long postingId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/admin/postings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postings[?(@.id==" + postingId + ")]").isNotEmpty())
                .andExpect(jsonPath("$.content[?(@.id==" + postingId + ")]").isNotEmpty());

        mockMvc.perform(get("/api/v1/public/postings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id==" + postingId + ")]").isEmpty());

        mockMvc.perform(post("/api/v1/admin/postings/{id}/publish", postingId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/public/postings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id==" + postingId + ")]").isNotEmpty());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginBody = """
                {
                  "email":"%s",
                  "password":"%s"
                }
                """.formatted(email, password);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.get("accessToken").asText();
    }

    private void seedAccount(String email, String rawPassword, Role role) {
        userAccountRepository.findByEmailIgnoreCase(email).ifPresent(userAccountRepository::delete);
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(role);
        account.setEnabled(true);
        account.setEmailVerified(true);
        userAccountRepository.save(account);
    }
}
