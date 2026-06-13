package com.project.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.UserAccountRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class PublicPostingVisibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationPostingRepository postingRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Long publishedId;
    private Long closedId;
    private Long draftId;

    @BeforeEach
    void setUp() {
        postingRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccount admin = new UserAccount();
        admin.setEmail("admin@32bit.com.tr");
        admin.setPasswordHash("x");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin = userAccountRepository.save(admin);

        publishedId = createPosting(admin, ApplicationPostingStatus.PUBLISHED, "Pub");
        closedId = createPosting(admin, ApplicationPostingStatus.CLOSED, "Closed");
        draftId = createPosting(admin, ApplicationPostingStatus.DRAFT, "Draft");
    }

    @Test
    void publicListReturnsPublishedAndClosedButNotDraft() throws Exception {
        mockMvc.perform(get("/api/v1/public/postings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postings.length()").value(2));
    }

    @Test
    void closedPostingDetailIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/public/postings/{id}", closedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void draftPostingDetailIsBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/public/postings/{id}", draftId))
                .andExpect(status().isForbidden());
    }

    private Long createPosting(UserAccount admin, ApplicationPostingStatus status, String title) {
        ApplicationPosting posting = new ApplicationPosting();
        posting.setCategory(ApplicationCategory.BACKEND);
        posting.setTitle(title);
        posting.setDescription("desc");
        posting.setProjectName("proj");
        posting.setProjectDetails("details");
        posting.setCreatedByAdmin(admin);
        posting.setStatus(status);
        return postingRepository.save(posting).getId();
    }
}
