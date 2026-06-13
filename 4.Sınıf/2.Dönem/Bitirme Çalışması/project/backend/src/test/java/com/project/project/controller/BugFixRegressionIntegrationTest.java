package com.project.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.UserAccountRepository;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
                "app.storage.root=./target/test-storage",
                "management.health.mail.enabled=false"
})
class BugFixRegressionIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserAccountRepository userAccountRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @MockBean
        private JavaMailSender javaMailSender;

        @Test
        void registerCvVisibleAndAdminCanDownloadViaSubmissionDetail() throws Exception {
                mockMailSender();
                seedAdmin("admin.bug@32bit.com.tr", "Admin12345!");

                Long userId = registerUserWithCv("cv.user@ogr.sakarya.edu.tr", "CvUser123!");
                enableAndVerify(userId);
                String userToken = login("cv.user@ogr.sakarya.edu.tr", "CvUser123!");
                String adminToken = login("admin.bug@32bit.com.tr", "Admin12345!");

                mockMvc.perform(get("/api/v1/users/me/documents")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.cvDocuments.length()").value(1))
                                .andExpect(jsonPath("$.cvDocuments[0].type").value("CV"));

                Long postingId = createAndPublishPosting(adminToken, "CV Posting");
                MvcResult submissionResult = mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + "}"))
                                .andExpect(status().isCreated())
                                .andReturn();
                Long submissionId = objectMapper.readTree(submissionResult.getResponse().getContentAsString())
                                .get("id").asLong();

                String detail = mockMvc.perform(get("/api/v1/admin/submissions/{id}", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").isString())
                                .andExpect(jsonPath("$.userEmail").value("cv.user@ogr.sakarya.edu.tr"))
                                .andExpect(jsonPath("$.userFirstName").value("Ali"))
                                .andExpect(jsonPath("$.userLastName").value("Kaya"))
                                .andExpect(jsonPath("$.cvDownloadUrl").isNotEmpty())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                String cvDownloadUrl = objectMapper.readTree(detail).get("cvDownloadUrl").asText();

                mockMvc.perform(get(apiPath(cvDownloadUrl)).header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Type",
                                                org.hamcrest.Matchers.containsString("application/pdf")))
                                .andExpect(header().string("Content-Disposition",
                                                org.hamcrest.Matchers.startsWith("inline;")));
        }

        @Test
        void publishedAttachmentPublicDownloadIsInlineAndSubmissionActionsWork() throws Exception {
                seedAdmin("admin.attachment@32bit.com.tr", "Admin12345!");
                seedUser("sub.user@ogr.sakarya.edu.tr", "User12345!");
                String adminToken = login("admin.attachment@32bit.com.tr", "Admin12345!");
                String userToken = login("sub.user@ogr.sakarya.edu.tr", "User12345!");

                Long postingId = createAndPublishPosting(adminToken, "Attachment Posting");

                MockMultipartFile file = new MockMultipartFile(
                                "files",
                                "guide.pdf",
                                "application/pdf",
                                "pdf-content".getBytes());
                MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/admin/postings/{id}/attachments", postingId)
                                .file(file)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isCreated())
                                .andReturn();
                Long attachmentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                                .get(0).get("id").asLong();

                mockMvc.perform(get("/api/v1/postings/{postingId}/attachments/{attachmentId}/download", postingId,
                                attachmentId))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Disposition",
                                                org.hamcrest.Matchers.startsWith("inline;")));

                MvcResult submissionResult = mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + "}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.postingTitle").value("Attachment Posting"))
                                .andExpect(jsonPath("$.postingCategory").value("BACKEND"))
                                .andReturn();
                Long submissionId = objectMapper.readTree(submissionResult.getResponse().getContentAsString())
                                .get("id").asLong();

                mockMvc.perform(post("/api/v1/admin/submissions/{id}/accept", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                Long userId = userAccountRepository.findByEmailIgnoreCase("sub.user@ogr.sakarya.edu.tr").orElseThrow()
                                .getId();
                String task = mockMvc.perform(post("/api/v1/admin/postings/{id}/tasks", postingId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Task 1","description":"Do work","dueDate":"2030-01-01"}
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long taskId = objectMapper.readTree(task).get("id").asLong();

                mockMvc.perform(post("/api/v1/admin/tasks/{taskId}/assign/{userId}", taskId, userId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"note\":\"initial\"}"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/me/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .param("postingId", postingId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].postingId").value(postingId));

                mockMvc.perform(post("/api/v1/admin/submissions/{id}/remove", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("REMOVED"));
        }

        @Test
        void adminAnnouncementsListUpdateDeleteAndRejectEndpointWorks() throws Exception {
                seedAdmin("admin.announce@32bit.com.tr", "Admin12345!");
                seedUser("reject.user@ogr.sakarya.edu.tr", "User12345!");
                String adminToken = login("admin.announce@32bit.com.tr", "Admin12345!");
                String userToken = login("reject.user@ogr.sakarya.edu.tr", "User12345!");

                String created = mockMvc.perform(post("/api/v1/admin/announcements")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Duyuru 1","content":"Icerik"}
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("Duyuru 1"))
                                .andReturn().getResponse().getContentAsString();
                Long announcementId = objectMapper.readTree(created).get("id").asLong();

                mockMvc.perform(get("/api/v1/admin/announcements")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").exists());

                mockMvc.perform(put("/api/v1/admin/announcements/{id}", announcementId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Duyuru 1G","content":"Guncel"}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Duyuru 1G"));

                mockMvc.perform(delete("/api/v1/admin/announcements/{id}", announcementId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNoContent());

                Long postingId = createAndPublishPosting(adminToken, "Reject Posting");
                Long submissionId = objectMapper.readTree(
                                mockMvc.perform(post("/api/v1/me/submissions")
                                                .header("Authorization", "Bearer " + userToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"postingId\":" + postingId + "}"))
                                                .andExpect(status().isCreated())
                                                .andReturn()
                                                .getResponse()
                                                .getContentAsString())
                                .get("id").asLong();

                mockMvc.perform(post("/api/v1/admin/submissions/{id}/reject", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        void adminUsersEndpointSupportsOptionalPaginationAndReturns200() throws Exception {
                seedAdmin("admin.userslist@32bit.com.tr", "Admin12345!");
                seedUser("userslist.user1@ogr.sakarya.edu.tr", "User12345!");
                seedUser("userslist.user2@ogr.sakarya.edu.tr", "User12345!");
                String adminToken = login("admin.userslist@32bit.com.tr", "Admin12345!");

                mockMvc.perform(get("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].id").exists())
                                .andExpect(jsonPath("$.content[0].email").exists())
                                .andExpect(jsonPath("$.content[0].role").exists())
                                .andExpect(jsonPath("$.content[0].enabled").exists());

                mockMvc.perform(get("/api/v1/admin/users")
                                .header("Authorization", "Bearer " + adminToken)
                                .param("page", "1")
                                .param("size", "1000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

        private void mockMailSender() {
                MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
                when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
                doNothing().when(javaMailSender).send(any(MimeMessage.class));
        }

        private Long registerUserWithCv(String email, String password) throws Exception {
                MockMultipartFile data = new MockMultipartFile(
                                "data",
                                "",
                                MediaType.APPLICATION_JSON_VALUE,
                                ("""
                                                {
                                                  "email":"%s",
                                                  "password":"%s",
                                                  "firstName":"Ali",
                                                  "lastName":"Kaya",
                                                  "classYear":3,
                                                  "department":"Computer Engineering",
                                                  "englishLevel":"B2",
                                                  "gpa":3.25
                                                }
                                                """.formatted(email, password)).getBytes());
                MockMultipartFile cv = new MockMultipartFile(
                                "cv",
                                "cv.pdf",
                                "application/pdf",
                                "dummy-pdf".getBytes());

                mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                                .andExpect(status().isCreated());
                return userAccountRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
        }

        private void seedAdmin(String email, String password) {
                userAccountRepository.findByEmailIgnoreCase(email).ifPresent(userAccountRepository::delete);
                UserAccount account = new UserAccount();
                account.setEmail(email);
                account.setPasswordHash(passwordEncoder.encode(password));
                account.setRole(Role.ADMIN);
                account.setEnabled(true);
                account.setEmailVerified(true);
                userAccountRepository.save(account);
        }

        private void seedUser(String email, String password) {
                userAccountRepository.findByEmailIgnoreCase(email).ifPresent(userAccountRepository::delete);
                UserAccount account = new UserAccount();
                account.setEmail(email);
                account.setPasswordHash(passwordEncoder.encode(password));
                account.setRole(Role.USER);
                account.setEnabled(true);
                account.setEmailVerified(true);
                userAccountRepository.save(account);
        }

        private void enableAndVerify(Long userId) {
                UserAccount account = userAccountRepository.findById(userId).orElseThrow();
                account.setEnabled(true);
                account.setEmailVerified(true);
                userAccountRepository.save(account);
        }

        private String login(String email, String password) throws Exception {
                String body = """
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password);
                String response = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                JsonNode root = objectMapper.readTree(response);
                return root.get("accessToken").asText();
        }

        private Long createAndPublishPosting(String adminToken, String title) throws Exception {
                String createBody = """
                                {
                                  "category":"BACKEND",
                                  "title":"%s",
                                  "description":"Desc",
                                  "projectName":"ATS",
                                  "projectDetails":"Details"
                                }
                                """.formatted(title);
                String createResponse = mockMvc.perform(post("/api/v1/admin/postings")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                Long postingId = objectMapper.readTree(createResponse).get("id").asLong();
                mockMvc.perform(post("/api/v1/admin/postings/{id}/publish", postingId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk());
                assertThat(postingId).isNotNull();
                return postingId;
        }

        private String apiPath(String relativePath) {
                if (relativePath == null || relativePath.isBlank()) {
                        return "/api/v1";
                }
                return relativePath.startsWith("/api/v1") ? relativePath : "/api/v1" + relativePath;
        }
}
