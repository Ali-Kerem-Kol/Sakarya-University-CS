package com.project.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
class TaskAssignmentWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void taskWorkflowCreateAssignSubmitReviewWorks() throws Exception {
        seedAccount("admin.taskwf@32bit.com.tr", "Admin12345!", Role.ADMIN);
        seedAccount("user.taskwf@ogr.sakarya.edu.tr", "User12345!", Role.USER);
        seedAccount("user2.taskwf@ogr.sakarya.edu.tr", "User12345!", Role.USER);
        String adminToken = login("admin.taskwf@32bit.com.tr", "Admin12345!");
        String userToken = login("user.taskwf@ogr.sakarya.edu.tr", "User12345!");
        String user2Token = login("user2.taskwf@ogr.sakarya.edu.tr", "User12345!");
        Long userId = userAccountRepository.findByEmailIgnoreCase("user.taskwf@ogr.sakarya.edu.tr").orElseThrow().getId();
        Long user2Id = userAccountRepository.findByEmailIgnoreCase("user2.taskwf@ogr.sakarya.edu.tr").orElseThrow().getId();

        Long projectId = createAndPublishPosting(adminToken, "Task Workflow Posting");

        String submission = mockMvc.perform(post("/api/v1/me/submissions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postingId\":" + projectId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long submissionId = objectMapper.readTree(submission).get("id").asLong();

        String submission2 = mockMvc.perform(post("/api/v1/me/submissions")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postingId\":" + projectId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long submission2Id = objectMapper.readTree(submission2).get("id").asLong();

        mockMvc.perform(post("/api/v1/admin/submissions/{id}/approve", submissionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/submissions/{id}/approve", submission2Id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        String createdMainTask = mockMvc.perform(post("/api/v1/admin/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"API Main Task",
                                  "description":"Project wide task",
                                  "assignMode":"ALL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long mainTaskId = objectMapper.readTree(createdMainTask).get("id").asLong();

        String createdTask = mockMvc.perform(post("/api/v1/admin/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"API Task 1",
                                  "description":"Implement endpoint",
                                  "assignMode":"USER",
                                  "assigneeUserId":%d
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignments[0].assignee.userId").value(userId))
                .andReturn().getResponse().getContentAsString();
        JsonNode taskRoot = objectMapper.readTree(createdTask);
        Long taskId = taskRoot.get("id").asLong();
        Long assignmentId = taskRoot.get("assignments").get(0).get("assignmentId").asLong();

        mockMvc.perform(post("/api/v1/admin/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"API Task 2",
                                  "description":"Implement endpoint for user2",
                                  "assignMode":"USER",
                                  "assigneeUserId":%d
                                }
                                """.formatted(user2Id)))
                .andExpect(status().isCreated());

        MockMultipartFile taskAttachment = new MockMultipartFile(
                "files",
                "task.pdf",
                "application/pdf",
                "task-pdf".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/admin/tasks/{taskId}/attachments", mainTaskId)
                        .file(taskAttachment)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].downloadUrl").value(org.hamcrest.Matchers.containsString(
                        "/api/v1/admin/tasks/" + mainTaskId + "/attachments/"
                )));

        mockMvc.perform(get("/api/v1/me/task-assignments")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assignmentId").value(assignmentId))
                .andExpect(jsonPath("$[0].status").value("ASSIGNED"));

        MockMultipartFile submitData = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"textAnswer\":\"Teslim metni\"}".getBytes()
        );
        MockMultipartFile submitFile = new MockMultipartFile(
                "files",
                "submission.pdf",
                "application/pdf",
                "submission-pdf".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/me/task-assignments/{assignmentId}/submit", assignmentId)
                        .file(submitData)
                        .file(submitFile)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.textAnswer").value("Teslim metni"));

        mockMvc.perform(get("/api/v1/admin/task-assignments/{assignmentId}", assignmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.email").value("user.taskwf@ogr.sakarya.edu.tr"))
                .andExpect(jsonPath("$.submissionFiles[0].fileName").value("submission.pdf"));

        mockMvc.perform(post("/api/v1/admin/task-assignments/{assignmentId}/review", assignmentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision":"REVISION_REQUESTED",
                                  "note":"Eksik bölüm var"
                                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVISION_REQUESTED"))
                .andExpect(jsonPath("$.reviewNote").value("Eksik bölüm var"));

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/timeline", projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventType").value("TASK_REVIEWED_REVISION"))
                .andExpect(jsonPath("$.content[1].eventType").value("TASK_SUBMITTED"))
                .andExpect(jsonPath("$.content[0].stats.assigned").value(4))
                .andExpect(jsonPath("$.content[0].actor.email").value("admin.taskwf@32bit.com.tr"));

        mockMvc.perform(get("/api/v1/admin/users/{userId}/timeline", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventType").value("TASK_REVIEWED_REVISION"))
                .andExpect(jsonPath("$.content[1].eventType").value("TASK_SUBMITTED"))
                .andExpect(jsonPath("$.content[0].assignment.status").value("REVISION_REQUESTED"));

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}/task-graph", projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.branches[0].branchKey").value("MAIN"))
                .andExpect(jsonPath("$.branches[?(@.branchKey=='USER-" + userId + "')]", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.branches[?(@.branchKey=='USER-" + user2Id + "')]", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.edges").isArray());

        mockMvc.perform(get("/api/v1/users/me/task-graph")
                        .header("Authorization", "Bearer " + userToken)
                        .param("projectId", String.valueOf(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branches[?(@.branchKey=='USER-" + user2Id + "')]", org.hamcrest.Matchers.empty()))
                .andExpect(jsonPath("$.nodes[?(@.branchKey!='MAIN' && @.branchKey!='USER-" + userId + "')]", org.hamcrest.Matchers.empty()))
                .andExpect(jsonPath("$.nodes[?(@.taskId==" + taskId + " && @.assignedToUserId==" + userId + ")]",
                        org.hamcrest.Matchers.hasSize(1)));

        mockMvc.perform(patch("/api/v1/admin/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"API Task 1 Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId));

        mockMvc.perform(post("/api/v1/admin/tasks/{taskId}/review", taskId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status":"SUCCESS",
                                  "assignedToUserId":%d,
                                  "reviewNote":"Tamam"
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(delete("/api/v1/admin/tasks/{taskId}", mainTaskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidTaskSubmissionMultipartJsonReturns400() throws Exception {
        seedAccount("admin.taskwf2@32bit.com.tr", "Admin12345!", Role.ADMIN);
        seedAccount("user.taskwf2@ogr.sakarya.edu.tr", "User12345!", Role.USER);
        String adminToken = login("admin.taskwf2@32bit.com.tr", "Admin12345!");
        String userToken = login("user.taskwf2@ogr.sakarya.edu.tr", "User12345!");
        Long userId = userAccountRepository.findByEmailIgnoreCase("user.taskwf2@ogr.sakarya.edu.tr").orElseThrow().getId();

        Long projectId = createAndPublishPosting(adminToken, "Task Workflow Posting 2");
        Long submissionId = objectMapper.readTree(
                mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + projectId + "}"))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/v1/admin/submissions/{id}/approve", submissionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        String createdTask = mockMvc.perform(post("/api/v1/admin/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"API Task invalid",
                                  "description":"Invalid multipart json",
                                  "assignMode":"USER",
                                  "assigneeUserId":%d
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long assignmentId = objectMapper.readTree(createdTask).get("assignments").get(0).get("assignmentId").asLong();

        MockMultipartFile invalidData = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"textAnswer\":".getBytes()
        );
        mockMvc.perform(multipart("/api/v1/me/task-assignments/{assignmentId}/submit", assignmentId)
                        .file(invalidData)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MULTIPART_JSON"));
    }

    @Test
    void disabledApprovedUserCannotBeAssignedToTask() throws Exception {
        seedAccount("admin.taskwf3@32bit.com.tr", "Admin12345!", Role.ADMIN);
        seedAccount("user.taskwf3@ogr.sakarya.edu.tr", "User12345!", Role.USER);
        String adminToken = login("admin.taskwf3@32bit.com.tr", "Admin12345!");
        String userToken = login("user.taskwf3@ogr.sakarya.edu.tr", "User12345!");
        Long userId = userAccountRepository.findByEmailIgnoreCase("user.taskwf3@ogr.sakarya.edu.tr")
                .orElseThrow()
                .getId();

        Long projectId = createAndPublishPosting(adminToken, "Task Workflow Posting 3");
        Long submissionId = objectMapper.readTree(
                mockMvc.perform(post("/api/v1/me/submissions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"postingId\":" + projectId + "}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/v1/admin/submissions/{id}/approve", submissionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        UserAccount account = userAccountRepository.findById(userId).orElseThrow();
        account.setEnabled(false);
        userAccountRepository.save(account);

        mockMvc.perform(post("/api/v1/admin/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Blocked assign USER",
                                  "description":"Must fail when assignee is disabled",
                                  "assignMode":"USER",
                                  "assigneeUserId":%d
                                }
                                """.formatted(userId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("USER_NOT_APPROVED_MEMBER"));

        mockMvc.perform(post("/api/v1/admin/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Blocked assign ALL",
                                  "description":"Must fail when no active approved users",
                                  "assignMode":"ALL"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No approved users found for project"));
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
