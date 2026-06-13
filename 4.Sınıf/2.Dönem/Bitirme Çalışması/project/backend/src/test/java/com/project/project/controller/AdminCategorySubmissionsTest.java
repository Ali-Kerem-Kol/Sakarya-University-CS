package com.project.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmission;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.security.UserAccountPrincipal;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class AdminCategorySubmissionsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ApplicationPostingRepository postingRepository;

    @Autowired
    private ApplicationSubmissionRepository submissionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private Long backendPostingId;

    @BeforeEach
    void setUp() {
        submissionRepository.deleteAll();
        postingRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccount admin = new UserAccount();
        admin.setEmail("admin@32bit.com.tr");
        admin.setPasswordHash("x");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin = userAccountRepository.save(admin);

        UserAccount user1 = new UserAccount();
        user1.setEmail("user1@ogr.sakarya.edu.tr");
        user1.setPasswordHash("x");
        user1.setRole(Role.USER);
        user1.setEnabled(true);
        user1.setEmailVerified(true);
        user1 = userAccountRepository.save(user1);

        UserAccount user2 = new UserAccount();
        user2.setEmail("user2@ogr.sakarya.edu.tr");
        user2.setPasswordHash("x");
        user2.setRole(Role.USER);
        user2.setEnabled(true);
        user2.setEmailVerified(true);
        user2 = userAccountRepository.save(user2);

        ApplicationPosting backend = createPosting(admin, ApplicationCategory.BACKEND, "Backend Post");
        ApplicationPosting mobile = createPosting(admin, ApplicationCategory.MOBILE, "Mobile Post");
        backendPostingId = backend.getId();

        createSubmission(backend, user1, "{\"firstName\":\"Ali\",\"lastName\":\"Kaya\",\"classYear\":3,"
                + "\"department\":\"CE\",\"englishLevel\":\"B2\",\"gpa\":3.20}");
        createSubmission(mobile, user2, "{\"firstName\":\"Ayse\",\"lastName\":\"Yilmaz\"}");
    }

    @Test
    void adminCanFilterSubmissionsByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/admin/submissions")
                        .param("category", "BACKEND")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].postingCategory").value("BACKEND"))
                .andExpect(jsonPath("$.content[0].userId").isString())
                .andExpect(jsonPath("$.content[0].userFirstName").value("Ali"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.content[0].user.id").isString())
                .andExpect(jsonPath("$.content[0].posting.id").isString())
                .andExpect(jsonPath("$.content[0].department").value("CE"));
    }

    @Test
    void adminCanFilterSubmissionsByPostingId() throws Exception {
        mockMvc.perform(get("/api/v1/admin/submissions")
                        .param("postingId", String.valueOf(backendPostingId))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].posting.id").value(String.valueOf(backendPostingId)))
                .andExpect(jsonPath("$.content[0].postingCategory").value("BACKEND"));
    }

    private ApplicationPosting createPosting(UserAccount admin, ApplicationCategory category, String title) {
        ApplicationPosting posting = new ApplicationPosting();
        posting.setCategory(category);
        posting.setTitle(title);
        posting.setDescription("desc");
        posting.setProjectName("proj");
        posting.setProjectDetails("details");
        posting.setStatus(ApplicationPostingStatus.PUBLISHED);
        posting.setCreatedByAdmin(admin);
        return postingRepository.save(posting);
    }

    private void createSubmission(ApplicationPosting posting, UserAccount user, String snapshotJson) {
        ApplicationSubmission submission = new ApplicationSubmission();
        submission.setPosting(posting);
        submission.setUser(user);
        submission.setStatus(ApplicationSubmissionStatus.PENDING);
        submission.setProfileSnapshotJson(snapshotJson);
        submission.setSnapshotVersion(1);
        submissionRepository.save(submission);
    }

    private UsernamePasswordAuthenticationToken authenticationAdmin() {
        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setEmail("admin@32bit.com.tr");
        account.setRole(Role.ADMIN);
        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
