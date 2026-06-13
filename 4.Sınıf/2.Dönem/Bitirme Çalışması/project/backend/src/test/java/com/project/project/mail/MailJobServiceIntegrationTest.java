package com.project.project.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.project.project.dto.mail.MailJobCreateRequest;
import com.project.project.dto.mail.MailJobResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmission;
import com.project.project.entity.MailJobStatus;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.MailJobRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.mail.MailJobService;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.storage.root=./target/test-storage"
})
class MailJobServiceIntegrationTest {

    @Autowired
    private MailJobService mailJobService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ApplicationPostingRepository postingRepository;

    @Autowired
    private ApplicationSubmissionRepository submissionRepository;

    @Autowired
    private MailJobRepository mailJobRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    private Long adminId;
    private Long postingId;

    @BeforeEach
    void setUp() {
        submissionRepository.deleteAll();
        postingRepository.deleteAll();
        mailJobRepository.deleteAll();
        userAccountRepository.deleteAll();
        long suffix = System.nanoTime();

        UserAccount admin = new UserAccount();
        admin.setEmail("admin" + suffix + "@32bit.com.tr");
        admin.setPasswordHash("x");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin = userAccountRepository.save(admin);
        adminId = admin.getId();

        UserAccount user = new UserAccount();
        user.setEmail("user" + suffix + "@ogr.sakarya.edu.tr");
        user.setPasswordHash("x");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user = userAccountRepository.save(user);

        ApplicationPosting posting = new ApplicationPosting();
        posting.setCategory(ApplicationCategory.BACKEND);
        posting.setTitle("Backend");
        posting.setDescription("Desc");
        posting.setProjectName("ATS");
        posting.setProjectDetails("Details");
        posting.setCreatedByAdmin(admin);
        posting.setStatus(ApplicationPostingStatus.PUBLISHED);
        posting.setPublishedAt(Instant.now());
        posting = postingRepository.save(posting);
        postingId = posting.getId();

        ApplicationSubmission submission = new ApplicationSubmission();
        submission.setPosting(posting);
        submission.setUser(user);
        submission.setProfileSnapshotJson("{\"firstName\":\"Ali\"}");
        submission.setSnapshotVersion(1);
        submissionRepository.save(submission);

        UserAccount disabledUser = new UserAccount();
        disabledUser.setEmail("disabled" + suffix + "@ogr.sakarya.edu.tr");
        disabledUser.setPasswordHash("x");
        disabledUser.setRole(Role.USER);
        disabledUser.setEnabled(false);
        disabledUser.setEmailVerified(true);
        disabledUser = userAccountRepository.save(disabledUser);
        ApplicationSubmission disabledSubmission = new ApplicationSubmission();
        disabledSubmission.setPosting(posting);
        disabledSubmission.setUser(disabledUser);
        disabledSubmission.setProfileSnapshotJson("{\"firstName\":\"Disabled\"}");
        disabledSubmission.setSnapshotVersion(1);
        submissionRepository.save(disabledSubmission);

        UserAccount unverifiedUser = new UserAccount();
        unverifiedUser.setEmail("unverified" + suffix + "@ogr.sakarya.edu.tr");
        unverifiedUser.setPasswordHash("x");
        unverifiedUser.setRole(Role.USER);
        unverifiedUser.setEnabled(true);
        unverifiedUser.setEmailVerified(false);
        unverifiedUser = userAccountRepository.save(unverifiedUser);
        ApplicationSubmission unverifiedSubmission = new ApplicationSubmission();
        unverifiedSubmission.setPosting(posting);
        unverifiedSubmission.setUser(unverifiedUser);
        unverifiedSubmission.setProfileSnapshotJson("{\"firstName\":\"Unverified\"}");
        unverifiedSubmission.setSnapshotVersion(1);
        submissionRepository.save(unverifiedSubmission);

        UserAccount wrongDomainUser = new UserAccount();
        wrongDomainUser.setEmail("corp" + suffix + "@32bit.com.tr");
        wrongDomainUser.setPasswordHash("x");
        wrongDomainUser.setRole(Role.USER);
        wrongDomainUser.setEnabled(true);
        wrongDomainUser.setEmailVerified(true);
        wrongDomainUser = userAccountRepository.save(wrongDomainUser);
        ApplicationSubmission wrongDomainSubmission = new ApplicationSubmission();
        wrongDomainSubmission.setPosting(posting);
        wrongDomainSubmission.setUser(wrongDomainUser);
        wrongDomainSubmission.setProfileSnapshotJson("{\"firstName\":\"Corp\"}");
        wrongDomainSubmission.setSnapshotVersion(1);
        submissionRepository.save(wrongDomainSubmission);

        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void postingMailJobTransitionsToProcessingOrDone() throws Exception {
        MailJobResponse job = mailJobService.createPostingJob(
                adminId,
                postingId,
                new MailJobCreateRequest("Hello", "Body"),
                java.util.List.of()
        );

        MailJobStatus finalStatus = waitForStatus(job.id(), Duration.ofSeconds(3));
        assertThat(finalStatus).isIn(MailJobStatus.PROCESSING, MailJobStatus.DONE);
        verify(javaMailSender, timeout(2000).times(1)).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void categoryMailJobTransitionsToProcessingOrDone() throws Exception {
        MailJobResponse job = mailJobService.createCategoryJob(
                adminId,
                ApplicationCategory.BACKEND,
                new MailJobCreateRequest("Hi", "Category body"),
                java.util.List.of()
        );

        MailJobStatus finalStatus = waitForStatus(job.id(), Duration.ofSeconds(3));
        assertThat(finalStatus).isIn(MailJobStatus.PROCESSING, MailJobStatus.DONE);
        verify(javaMailSender, timeout(2000).times(1)).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void allStudentsMailJobIncludesEligibleStudentsWithoutSubmission() throws Exception {
        long suffix = System.nanoTime();
        UserAccount noSubmissionUser = new UserAccount();
        noSubmissionUser.setEmail("nosub" + suffix + "@ogr.sakarya.edu.tr");
        noSubmissionUser.setPasswordHash("x");
        noSubmissionUser.setRole(Role.USER);
        noSubmissionUser.setEnabled(true);
        noSubmissionUser.setEmailVerified(true);
        userAccountRepository.save(noSubmissionUser);

        MailJobResponse job = mailJobService.createAllStudentsJob(
                adminId,
                new MailJobCreateRequest("All", "Students"),
                java.util.List.of()
        );

        MailJobStatus finalStatus = waitForStatus(job.id(), Duration.ofSeconds(3));
        assertThat(finalStatus).isIn(MailJobStatus.PROCESSING, MailJobStatus.DONE);
        verify(javaMailSender, timeout(2000).times(2)).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    private MailJobStatus waitForStatus(Long jobId, Duration timeout) throws InterruptedException {
        long end = System.currentTimeMillis() + timeout.toMillis();
        MailJobStatus status = MailJobStatus.PENDING;
        while (System.currentTimeMillis() < end) {
            status = mailJobRepository.findById(jobId).orElseThrow().getStatus();
            if (status == MailJobStatus.DONE || status == MailJobStatus.FAILED || status == MailJobStatus.PROCESSING) {
                return status;
            }
            Thread.sleep(100L);
        }
        return status;
    }
}
