package com.project.project.posting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.project.project.dto.posting.AttachmentSummaryResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.PostingAttachmentRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.posting.PostingAttachmentService;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.storage.root=./target/test-storage"
})
class PostingAttachmentMutationPolicyTest {

    @Autowired
    private PostingAttachmentService postingAttachmentService;

    @Autowired
    private PostingAttachmentRepository postingAttachmentRepository;

    @Autowired
    private ApplicationPostingRepository postingRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Long adminId;
    private Long publishedPostingId;
    private Long closedPostingId;

    @BeforeEach
    void setUp() {
        postingAttachmentRepository.deleteAll();
        postingRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccount admin = new UserAccount();
        admin.setEmail("admin@32bit.com.tr");
        admin.setPasswordHash("x");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin = userAccountRepository.save(admin);
        adminId = admin.getId();

        publishedPostingId = createPosting(admin, ApplicationPostingStatus.PUBLISHED, "Pub");
        closedPostingId = createPosting(admin, ApplicationPostingStatus.CLOSED, "Closed");
    }

    @Test
    void uploadAllowedWhenPostingPublished() {
        MockMultipartFile file = pdfFile("spec.pdf");
        List<AttachmentSummaryResponse> responses = postingAttachmentService.upload(adminId, publishedPostingId, List.of(file));
        assertThat(responses).hasSize(1);
    }

    @Test
    void uploadBlockedWhenPostingClosed() {
        MockMultipartFile file = pdfFile("spec.pdf");
        assertThatThrownBy(() -> postingAttachmentService.upload(adminId, closedPostingId, List.of(file)))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Long createPosting(UserAccount admin, ApplicationPostingStatus status, String title) {
        ApplicationPosting posting = new ApplicationPosting();
        posting.setCategory(ApplicationCategory.BACKEND);
        posting.setTitle(title);
        posting.setDescription("desc");
        posting.setProjectName("ATS");
        posting.setProjectDetails("details");
        posting.setCreatedByAdmin(admin);
        posting.setStatus(status);
        return postingRepository.save(posting).getId();
    }

    private MockMultipartFile pdfFile(String fileName) {
        return new MockMultipartFile(
                "files",
                fileName,
                "application/pdf",
                "dummy-pdf".getBytes(StandardCharsets.UTF_8)
        );
    }
}
