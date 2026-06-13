package com.project.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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
import com.project.project.security.UserAccountPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class SubmissionClosedPostingPolicyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationPostingRepository postingRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Long closedPostingId;
    private Long userId;

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

        UserAccount user = new UserAccount();
        user.setEmail("student@ogr.sakarya.edu.tr");
        user.setPasswordHash("x");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user = userAccountRepository.save(user);
        userId = user.getId();

        ApplicationPosting posting = new ApplicationPosting();
        posting.setCategory(ApplicationCategory.BACKEND);
        posting.setTitle("Closed");
        posting.setDescription("desc");
        posting.setProjectName("p");
        posting.setProjectDetails("d");
        posting.setStatus(ApplicationPostingStatus.CLOSED);
        posting.setCreatedByAdmin(admin);
        closedPostingId = postingRepository.save(posting).getId();
    }

    @Test
    void createSubmissionForClosedPostingReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/me/submissions")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationUser(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postingId\":" + closedPostingId + "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    private UsernamePasswordAuthenticationToken authenticationUser(Long id) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setEmail("u" + id + "@ogr.sakarya.edu.tr");
        account.setRole(Role.USER);
        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
