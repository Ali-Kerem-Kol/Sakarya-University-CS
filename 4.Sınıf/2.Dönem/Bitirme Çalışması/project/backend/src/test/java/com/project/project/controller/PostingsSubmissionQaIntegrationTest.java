package com.project.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class PostingsSubmissionQaIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserAccountRepository userAccountRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void closedPostingNotVisibleInPublicListOrDetail() throws Exception {
                seedAccount("admin.closed@32bit.com.tr", "Admin12345!", Role.ADMIN);
                String adminToken = login("admin.closed@32bit.com.tr", "Admin12345!");
                Long postingId = createAndPublishPosting(adminToken, "Closed Visibility");

                mockMvc.perform(post("/api/v1/admin/postings/{id}/close", postingId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CLOSED"));

                mockMvc.perform(get("/api/v1/public/postings"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id==" + postingId + ")]").isEmpty());

                mockMvc.perform(get("/api/v1/public/postings/{id}", postingId))
                                .andExpect(status().isForbidden());

                mockMvc.perform(post("/api/v1/admin/postings/{id}/reopen", postingId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PUBLISHED"));

                mockMvc.perform(get("/api/v1/public/postings"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id==" + postingId + ")]").isNotEmpty());
        }

        @Test
        void pendingAcceptFlowCreatesMemberAndRemoveDropsMembership() throws Exception {
                seedAccount("admin.member@32bit.com.tr", "Admin12345!", Role.ADMIN);
                seedAccount("user.member@ogr.sakarya.edu.tr", "User12345!", Role.USER);
                String adminToken = login("admin.member@32bit.com.tr", "Admin12345!");
                String userToken = login("user.member@ogr.sakarya.edu.tr", "User12345!");

                Long postingId = createAndPublishPosting(adminToken, "Member Flow Posting");

                String submission = mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + "}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.id").isString())
                                .andExpect(jsonPath("$.postingId").isString())
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andReturn().getResponse().getContentAsString();
                Long submissionId = objectMapper.readTree(submission).get("id").asLong();

                mockMvc.perform(post("/api/v1/admin/submissions/{id}/approve", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                String task = mockMvc.perform(post("/api/v1/admin/postings/{id}/tasks", postingId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Task 1","description":"Do task","dueDate":"2030-01-01"}
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long taskId = objectMapper.readTree(task).get("id").asLong();
                Long userId = userAccountRepository.findByEmailIgnoreCase("user.member@ogr.sakarya.edu.tr")
                                .orElseThrow().getId();

                mockMvc.perform(post("/api/v1/admin/tasks/{taskId}/assign/{userId}", taskId, userId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"note\":\"initial\"}"))
                                .andExpect(status().isOk());

                mockMvc.perform(delete("/api/v1/admin/submissions/{id}", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("REMOVED"));

                mockMvc.perform(post("/api/v1/admin/tasks/{taskId}/assign/{userId}", taskId, userId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"note\":\"reassign\"}"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value("USER_NOT_APPROVED_MEMBER"));
        }

        @Test
        void projectOnlyQuestionsRequireSubmission() throws Exception {
                seedAccount("admin.qa@32bit.com.tr", "Admin12345!", Role.ADMIN);
                seedAccount("user.qa1@ogr.sakarya.edu.tr", "User12345!", Role.USER);
                seedAccount("user.qa2@ogr.sakarya.edu.tr", "User12345!", Role.USER);
                String adminToken = login("admin.qa@32bit.com.tr", "Admin12345!");
                String user1Token = login("user.qa1@ogr.sakarya.edu.tr", "User12345!");
                String user2Token = login("user.qa2@ogr.sakarya.edu.tr", "User12345!");
                Long postingId = createAndPublishPosting(adminToken, "QA Posting");

                mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + user1Token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + "}"))
                                .andExpect(status().isCreated());

                String question = mockMvc.perform(post("/api/v1/me/postings/{id}/questions", postingId)
                                .header("Authorization", "Bearer " + user1Token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"questionText\":\"Ne zaman başlayacak?\"}"))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long questionId = objectMapper.readTree(question).get("id").asLong();

                mockMvc.perform(post("/api/v1/admin/questions/{id}/answer", questionId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"answerText\":\"Gelecek hafta\"}"))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/v1/admin/questions/{id}/publish", questionId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"scope\":\"PROJECT_ONLY\"}"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/postings/{id}/questions", postingId)
                                .header("Authorization", "Bearer " + user2Token))
                                .andExpect(status().isForbidden());

                mockMvc.perform(get("/api/v1/postings/{id}/questions", postingId)
                                .header("Authorization", "Bearer " + user1Token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].questionText").value("Ne zaman başlayacak?"))
                                .andExpect(jsonPath("$[0].answerText").value("Gelecek hafta"));
        }

        @Test
        void deletePendingSubmissionMarksCanceled() throws Exception {
                seedAccount("admin.cancel@32bit.com.tr", "Admin12345!", Role.ADMIN);
                seedAccount("user.cancel@ogr.sakarya.edu.tr", "User12345!", Role.USER);
                String adminToken = login("admin.cancel@32bit.com.tr", "Admin12345!");
                String userToken = login("user.cancel@ogr.sakarya.edu.tr", "User12345!");

                Long postingId = createAndPublishPosting(adminToken, "Cancel Flow Posting");
                String submission = mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + "}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn().getResponse().getContentAsString();
                Long submissionId = objectMapper.readTree(submission).get("id").asLong();

                mockMvc.perform(delete("/api/v1/admin/submissions/{id}", submissionId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELED"));
        }

        @Test
        void publicQaEndpointReturnsOnlyPublishedAndAnonymous() throws Exception {
                seedAccount("admin.qapub@32bit.com.tr", "Admin12345!", Role.ADMIN);
                seedAccount("user.qapub@ogr.sakarya.edu.tr", "User12345!", Role.USER);
                String adminToken = login("admin.qapub@32bit.com.tr", "Admin12345!");
                String userToken = login("user.qapub@ogr.sakarya.edu.tr", "User12345!");
                Long postingId = createAndPublishPosting(adminToken, "Public QA Posting");

                String q = mockMvc.perform(post("/api/v1/questions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + ",\"questionText\":\"Soru 1\"}"))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long questionId = objectMapper.readTree(q).get("id").asLong();

                mockMvc.perform(get("/api/v1/postings/{id}/qa", postingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isEmpty());

                mockMvc.perform(post("/api/v1/admin/questions/{id}/answer", questionId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"answerText\":\"Cevap 1\"}"))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/v1/admin/questions/{id}/publish", questionId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"published\":true}"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/postings/{id}/qa", postingId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].questionText").value("Soru 1"))
                                .andExpect(jsonPath("$[0].answerText").value("Cevap 1"))
                                .andExpect(jsonPath("$[0].askedByUserId").doesNotExist())
                                .andExpect(jsonPath("$[0].askedByEmail").doesNotExist());
        }

        @Test
        void myQuestionsShowsAnswerStateWithIsPublished() throws Exception {
                seedAccount("admin.myq@32bit.com.tr", "Admin12345!", Role.ADMIN);
                seedAccount("user.myq@ogr.sakarya.edu.tr", "User12345!", Role.USER);
                String adminToken = login("admin.myq@32bit.com.tr", "Admin12345!");
                String userToken = login("user.myq@ogr.sakarya.edu.tr", "User12345!");
                Long postingId = createAndPublishPosting(adminToken, "My Questions Posting");

                String q = mockMvc.perform(post("/api/v1/questions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + ",\"questionText\":\"Durum nedir?\"}"))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long questionId = objectMapper.readTree(q).get("id").asLong();

                mockMvc.perform(get("/api/v1/questions/my")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].questionText").value("Durum nedir?"))
                                .andExpect(jsonPath("$[0].answerText").isEmpty())
                                .andExpect(jsonPath("$[0].isPublished").value(false));

                mockMvc.perform(post("/api/v1/admin/questions/{id}/answer", questionId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"answerText\":\"Açıklandı\"}"))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/v1/admin/questions/{id}/publish", questionId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"published\":true}"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/questions/my")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].answerText").value("Açıklandı"))
                                .andExpect(jsonPath("$[0].isPublished").value(true));
        }

        @Test
        void adminCannotUseStudentQuestionCreateEndpoint() throws Exception {
                seedAccount("admin.only@32bit.com.tr", "Admin12345!", Role.ADMIN);
                String adminToken = login("admin.only@32bit.com.tr", "Admin12345!");
                Long postingId = createAndPublishPosting(adminToken, "Student-only QA");

                mockMvc.perform(post("/api/v1/questions")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + postingId + ",\"questionText\":\"Admin soru\"}"))
                                .andExpect(status().isForbidden());
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
                return postingId;
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

        private void seedAccount(String email, String password, Role role) {
                userAccountRepository.findByEmailIgnoreCase(email).ifPresent(userAccountRepository::delete);
                UserAccount account = new UserAccount();
                account.setEmail(email);
                account.setPasswordHash(passwordEncoder.encode(password));
                account.setRole(role);
                account.setEnabled(true);
                account.setEmailVerified(true);
                userAccountRepository.save(account);
        }
}
