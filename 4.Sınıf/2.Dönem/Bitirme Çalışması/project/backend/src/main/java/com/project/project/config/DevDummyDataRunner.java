package com.project.project.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmission;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.entity.ProjectTask;
import com.project.project.entity.Role;
import com.project.project.entity.TaskAssignment;
import com.project.project.entity.TaskAssignmentStatus;
import com.project.project.entity.TaskStatus;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.dto.task.TaskAssignMode;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.ProjectTaskRepository;
import com.project.project.repository.TaskAssignmentRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.task.TaskTimelineService;

/**
 * Seeds realistic dummy data for local dev/test scenarios when explicitly enabled.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DevDummyDataRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevDummyDataRunner.class);

    private static final String STUDENT_PASSWORD = "12345678";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@32bit.com.tr";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin12345!";

    private static final StudentSeed[] STUDENTS = new StudentSeed[] {
            new StudentSeed("ali.yilmaz@ogr.sakarya.edu.tr", "Ali", "Yilmaz", "Computer Engineering", 4, "3.35", "B2"),
            new StudentSeed("ayse.demir@ogr.sakarya.edu.tr", "Ayse", "Demir", "Software Engineering", 3, "3.10", "B2"),
            new StudentSeed("mehmet.kaya@ogr.sakarya.edu.tr", "Mehmet", "Kaya", "Computer Engineering", 4, "2.95", "B1"),
            new StudentSeed("zeynep.sahin@ogr.sakarya.edu.tr", "Zeynep", "Sahin", "Information Systems", 2, "3.55", "C1"),
            new StudentSeed("emre.celik@ogr.sakarya.edu.tr", "Emre", "Celik", "Computer Engineering", 3, "2.75", "B1"),
            new StudentSeed("selin.akar@ogr.sakarya.edu.tr", "Selin", "Akar", "Software Engineering", 4, "3.62", "C1")
    };

    private static final PostingSeed[] POSTINGS = new PostingSeed[] {
            new PostingSeed(ApplicationCategory.BACKEND, "Backend API Platform - Spring", "Order and inventory microservice APIs", "Retail API Suite", "Spring Boot, PostgreSQL, caching and observability."),
            new PostingSeed(ApplicationCategory.BACKEND, "Backend Data Pipeline - Java", "Event ingestion and ETL services", "Analytics Pipeline", "Kafka consumers, batch jobs and reporting APIs."),
            new PostingSeed(ApplicationCategory.BACKEND, "Backend Integration Gateway - Java", "Unified gateway and auth delegation services", "Gateway Core", "API gateway, rate limit, auth delegation and audit logs."),
            new PostingSeed(ApplicationCategory.FRONTEND, "Frontend Admin Console - React", "Admin panel for operations teams", "Ops Console", "React + TS with dashboard and form-heavy workflows."),
            new PostingSeed(ApplicationCategory.FRONTEND, "Frontend Candidate Portal - React", "Candidate-facing job application UI", "Candidate Portal", "Document upload, profile forms and interview timeline."),
            new PostingSeed(ApplicationCategory.FRONTEND, "Frontend Workflow Studio - React", "Visual workflow builder for recruiters", "Workflow Studio", "Drag-drop flows, preview, role-based controls and audit history."),
            new PostingSeed(ApplicationCategory.MOBILE, "Mobile Interview Tracker - Flutter", "Cross-platform interview tracker app", "Interview Tracker", "Notifications, calendar sync and offline-first screens."),
            new PostingSeed(ApplicationCategory.MOBILE, "Mobile HR Assistant - React Native", "HR helper app for managers", "HR Assistant", "Task reminders, approvals and summary cards."),
            new PostingSeed(ApplicationCategory.MOBILE, "Mobile Candidate Companion - Flutter", "Candidate guidance mobile assistant", "Candidate Companion", "Interview prep cards, reminders and progress insights."),
            new PostingSeed(ApplicationCategory.FULLSTACK, "Fullstack ATS Core - Web", "End-to-end ATS workflow project", "ATS Core", "Backend, frontend, auth, documents and notifications."),
            new PostingSeed(ApplicationCategory.FULLSTACK, "Fullstack Internship Hub - Web", "Student-company matching platform", "Internship Hub", "Search, matching, messaging and admin controls."),
            new PostingSeed(ApplicationCategory.FULLSTACK, "Fullstack Hiring Insights - Web", "Hiring analytics and decision support platform", "Hiring Insights", "Data pipeline, dashboard, filters, exports and review workflows.")
    };
    private static final String[] TASK_TITLE_PARTS = new String[] {
            "Kickoff ve Kapsam",
            "Mimari Taslak",
            "Veri Modeli",
            "Entegrasyon",
            "CI/CD ve Kalite",
            "UI Akis Iyilestirme",
            "Dokuman ve Teslim",
            "Performans Optimizasyonu",
            "Test Otomasyonu",
            "Release Hazirligi",
            "Postmortem",
            "Operasyonel Izleme"
    };
    private static final String[] TASK_DESCRIPTION_PARTS = new String[] {
            "Hedefler, teslim plani ve risklerin netlestirilmesi.",
            "Teknik kararlar, veri akislari ve implementasyon plani.",
            "Uygulama akislarinin stabil calismasi icin revizyonlar.",
            "Dokuman, checklist ve teknik borclarin azaltilmasi.",
            "Kalite olcumleri ve gozden gecirme notlarinin tamamlanmasi."
    };
    private static final String[] PERSONAL_TASK_PARTS = new String[] {
            "API Prototype",
            "Bugfix Sprint",
            "Feature Gelistirme",
            "Test Paketi",
            "Raporlama ve Dokuman",
            "Performans Iyilestirme",
            "Veri Duzeltme",
            "CI Kural Guncelleme"
    };

    private final Environment environment;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final ApplicationPostingRepository postingRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskTimelineService taskTimelineService;

    public DevDummyDataRunner(
            Environment environment,
            PasswordEncoder passwordEncoder,
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            ApplicationPostingRepository postingRepository,
            ApplicationSubmissionRepository submissionRepository,
            ProjectTaskRepository projectTaskRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            TaskTimelineService taskTimelineService
    ) {
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.postingRepository = postingRepository;
        this.submissionRepository = submissionRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskTimelineService = taskTimelineService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isSeedEnabled() || !isDevOrTestProfileActive()) {
            return;
        }

        logger.info("SEED: started");

        UserAccount admin = ensureAdminExists();
        List<UserAccount> students = Arrays.stream(STUDENTS)
                .map(this::upsertStudent)
                .toList();

        for (int postingIndex = 0; postingIndex < POSTINGS.length; postingIndex++) {
            PostingSeed postingSeed = POSTINGS[postingIndex];
            ApplicationPosting posting = upsertPosting(admin, postingSeed);

            List<UserAccount> approvedUsers = assignSubmissionMix(posting, students, postingIndex);
            createTaskScenario(posting, admin, approvedUsers, postingIndex);
        }

        logger.info("SEED: done");
    }

    private boolean isDevOrTestProfileActive() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile));
    }

    private boolean isSeedEnabled() {
        String raw = environment.getProperty("SEED_DATA", environment.getProperty("seed.data", "false"));
        return Boolean.parseBoolean(raw);
    }

    private UserAccount ensureAdminExists() {
        return userAccountRepository.findFirstByRoleOrderByIdAsc(Role.ADMIN)
                .orElseGet(this::createFallbackAdmin);
    }

    private UserAccount createFallbackAdmin() {
        String email = environment.getProperty("SEED_ADMIN_EMAIL", DEFAULT_ADMIN_EMAIL);
        String password = environment.getProperty("SEED_ADMIN_PASSWORD", DEFAULT_ADMIN_PASSWORD);
        String firstName = environment.getProperty("SEED_ADMIN_FIRSTNAME", "System");
        String lastName = environment.getProperty("SEED_ADMIN_LASTNAME", "Administrator");

        UserAccount account = userAccountRepository.findByEmailIgnoreCase(email).orElseGet(UserAccount::new);
        account.setEmail(email);
        account.setRole(Role.ADMIN);
        account.setEnabled(true);
        account.setEmailVerified(true);
        if (account.getPasswordHash() == null || !passwordEncoder.matches(password, account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(password));
        }
        UserAccount saved = userAccountRepository.save(account);

        UserProfile profile = userProfileRepository.findByUserAccountId(saved.getId()).orElseGet(UserProfile::new);
        profile.setUserAccount(saved);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        userProfileRepository.save(profile);
        logger.info("SEED: created fallback admin {}", email);
        return saved;
    }

    private UserAccount upsertStudent(StudentSeed seed) {
        UserAccount account = userAccountRepository.findByEmailIgnoreCase(seed.email()).orElseGet(UserAccount::new);
        boolean isNew = account.getId() == null;

        account.setEmail(seed.email());
        account.setRole(Role.USER);
        account.setEnabled(true);
        account.setEmailVerified(true);
        if (account.getPasswordHash() == null || !passwordEncoder.matches(STUDENT_PASSWORD, account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(STUDENT_PASSWORD));
        }
        UserAccount savedAccount = userAccountRepository.save(account);
        logCreateOrExists("user", seed.email(), isNew);

        UserProfile profile = userProfileRepository.findByUserAccountId(savedAccount.getId()).orElseGet(UserProfile::new);
        profile.setUserAccount(savedAccount);
        profile.setFirstName(seed.firstName());
        profile.setLastName(seed.lastName());
        profile.setDepartment(seed.department());
        profile.setClassYear(seed.classYear());
        profile.setGpa(new BigDecimal(seed.gpa()));
        profile.setEnglishLevel(seed.englishLevel());
        profile.setPhoneNumber("5550000000");
        userProfileRepository.save(profile);
        return savedAccount;
    }

    private ApplicationPosting upsertPosting(UserAccount admin, PostingSeed seed) {
        ApplicationPosting posting = postingRepository.findByTitleIgnoreCase(seed.title()).orElseGet(ApplicationPosting::new);
        boolean isNew = posting.getId() == null;

        posting.setCategory(seed.category());
        posting.setTitle(seed.title());
        posting.setDescription(seed.description());
        posting.setProjectName(seed.projectName());
        posting.setProjectDetails(seed.projectDetails());
        posting.setCreatedByAdmin(admin);
        if (posting.getStatus() != ApplicationPostingStatus.PUBLISHED) {
            posting.changeStatus(ApplicationPostingStatus.PUBLISHED);
        }
        ApplicationPosting saved = postingRepository.save(posting);
        logCreateOrExists("posting", seed.title(), isNew);
        return saved;
    }

    private List<UserAccount> assignSubmissionMix(ApplicationPosting posting, List<UserAccount> students, int postingIndex) {
        SplittableRandom random = randomForPosting(postingIndex, 11);
        List<UserAccount> shuffled = new ArrayList<>(students);
        Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));

        int applicantCount = shuffled.size();
        int minApproved = Math.min(applicantCount, Math.max(2, applicantCount - 2));
        int approvedCount = minApproved + random.nextInt(applicantCount - minApproved + 1);
        int remaining = applicantCount - approvedCount;
        int rejectedCount = remaining == 0 ? 0 : random.nextInt(Math.min(2, remaining) + 1);
        int pendingCount = remaining - rejectedCount;

        List<UserAccount> approvedUsers = new ArrayList<>();
        for (int i = 0; i < applicantCount; i++) {
            UserAccount user = shuffled.get(i);
            if (i < approvedCount) {
                upsertSubmission(posting, user, ApplicationSubmissionStatus.APPROVED);
                approvedUsers.add(user);
                continue;
            }
            if (i < approvedCount + rejectedCount) {
                upsertSubmission(posting, user, ApplicationSubmissionStatus.REJECTED);
                continue;
            }
            if (pendingCount > 0) {
                upsertSubmission(posting, user, ApplicationSubmissionStatus.PENDING);
            }
        }
        return approvedUsers;
    }

    private void createTaskScenario(
            ApplicationPosting posting,
            UserAccount admin,
            List<UserAccount> approvedUsers,
            int postingIndex
    ) {
        if (approvedUsers.isEmpty()) {
            return;
        }

        SplittableRandom random = randomForPosting(postingIndex, 29);
        List<UserAccount> team = new ArrayList<>(approvedUsers);
        Collections.shuffle(team, new java.util.Random(random.nextLong()));
        int dayCursor = 2 + random.nextInt(3);
        int mainTaskIndex = 1;
        int personalTaskIndex = 1;

        int stageCount = 10 + random.nextInt(9); // 10-18 tasks per posting
        for (int stage = 0; stage < stageCount; stage++) {
            boolean createMainTask = stage < 2 || random.nextDouble() < 0.58;
            if (createMainTask) {
                TaskSeedResult seedResult = upsertTask(
                        posting,
                        admin,
                        "Ana Gorev " + mainTaskIndex++ + " - " + TASK_TITLE_PARTS[random.nextInt(TASK_TITLE_PARTS.length)],
                        posting.getProjectName() + " projesi icin " + TASK_DESCRIPTION_PARTS[random.nextInt(TASK_DESCRIPTION_PARTS.length)],
                        LocalDate.now().plusDays(dayCursor + random.nextInt(4))
                );
                List<TaskAssignment> assignments = assignTaskToAllApproved(seedResult.task(), team, random);
                if (seedResult.created()) {
                    taskTimelineService.recordTaskCreated(admin, seedResult.task(), TaskAssignMode.ALL, assignments);
                }
                dayCursor += 1 + random.nextInt(3);
                continue;
            }

            UserAccount owner = team.get(random.nextInt(team.size()));
            String firstName = resolveFirstName(owner);
            TaskSeedResult seedResult = upsertTask(
                    posting,
                    admin,
                    "Kisiye Ozel " + personalTaskIndex++ + " - " + firstName + " - " +
                            PERSONAL_TASK_PARTS[random.nextInt(PERSONAL_TASK_PARTS.length)],
                    firstName + " icin ozel sorumluluk: " +
                            TASK_DESCRIPTION_PARTS[random.nextInt(TASK_DESCRIPTION_PARTS.length)],
                    LocalDate.now().plusDays(dayCursor + random.nextInt(5))
            );
            TaskAssignmentStatus status = randomPersonalAssignmentStatus(random);
            String textAnswer = status == TaskAssignmentStatus.ASSIGNED
                    ? null
                    : "Calisma notu: " + firstName + " bireysel gorev teslimi guncellendi.";
            String reviewNote = switch (status) {
                case APPROVED -> "Bireysel teslim kabul edildi.";
                case DONE -> "Bireysel gorev kapatildi.";
                case REVISION_REQUESTED -> "Detaylandirma gerekli, revizyon istendi.";
                case REJECTED -> "Teslim kalite kontrolunden gecemedi.";
                case FAILED -> "Teknik kriterler saglanamadi.";
                default -> null;
            };
            TaskAssignment assignment = upsertAssignment(seedResult.task(), owner, status, textAnswer, reviewNote);
            if (seedResult.created()) {
                taskTimelineService.recordTaskCreated(admin, seedResult.task(), TaskAssignMode.USER, List.of(assignment));
            }
            dayCursor += 1 + random.nextInt(2);
        }
    }

    private TaskAssignmentStatus randomAssignmentStatus(SplittableRandom random) {
        int roll = random.nextInt(100);
        if (roll < 18) {
            return TaskAssignmentStatus.ASSIGNED;
        }
        if (roll < 39) {
            return TaskAssignmentStatus.SUBMITTED;
        }
        if (roll < 57) {
            return TaskAssignmentStatus.DONE;
        }
        if (roll < 75) {
            return TaskAssignmentStatus.APPROVED;
        }
        if (roll < 85) {
            return TaskAssignmentStatus.REVISION_REQUESTED;
        }
        if (roll < 93) {
            return TaskAssignmentStatus.REJECTED;
        }
        return TaskAssignmentStatus.FAILED;
    }

    private TaskAssignmentStatus randomPersonalAssignmentStatus(SplittableRandom random) {
        int roll = random.nextInt(100);
        if (roll < 14) {
            return TaskAssignmentStatus.ASSIGNED;
        }
        if (roll < 34) {
            return TaskAssignmentStatus.SUBMITTED;
        }
        if (roll < 50) {
            return TaskAssignmentStatus.DONE;
        }
        if (roll < 66) {
            return TaskAssignmentStatus.APPROVED;
        }
        if (roll < 80) {
            return TaskAssignmentStatus.REVISION_REQUESTED;
        }
        if (roll < 91) {
            return TaskAssignmentStatus.REJECTED;
        }
        return TaskAssignmentStatus.FAILED;
    }

    private List<TaskAssignment> assignTaskToAllApproved(
            ProjectTask task,
            List<UserAccount> team,
            SplittableRandom random
    ) {
        List<TaskAssignment> savedAssignments = new ArrayList<>();
        for (UserAccount user : team) {
            TaskAssignmentStatus status = randomAssignmentStatus(random);
            String textAnswer = status == TaskAssignmentStatus.ASSIGNED
                    ? null
                    : "Calisma notu: " + resolveFirstName(user) + " teslimini guncelledi.";
            String reviewNote = switch (status) {
                case APPROVED -> "Inceleme tamamlandi, teslim kabul edildi.";
                case DONE -> "Gorev kapatildi, kalite kriterleri saglandi.";
                case REVISION_REQUESTED -> "Eksik noktalar var, revizyon gerekiyor.";
                case REJECTED -> "Teslim reddedildi, beklenen kriterler saglanmadi.";
                case FAILED -> "Teslim teknik kalite esiklerini gecemedi.";
                default -> null;
            };
            savedAssignments.add(upsertAssignment(task, user, status, textAnswer, reviewNote));
        }
        return savedAssignments;
    }

    private SplittableRandom randomForPosting(int postingIndex, int salt) {
        long seed = 2_024_03_03L + (long) postingIndex * 9_973L + salt;
        return new SplittableRandom(seed);
    }

    private String resolveFirstName(UserAccount user) {
        UserProfile profile = user.getProfile() != null
                ? user.getProfile()
                : userProfileRepository.findByUserAccountId(user.getId()).orElse(null);
        if (profile == null || profile.getFirstName() == null || profile.getFirstName().isBlank()) {
            return "Kullanici";
        }
        return profile.getFirstName();
    }

    private void upsertSubmission(ApplicationPosting posting, UserAccount student, ApplicationSubmissionStatus status) {
        ApplicationSubmission submission = submissionRepository.findByPostingIdAndUserId(posting.getId(), student.getId())
                .orElseGet(ApplicationSubmission::new);
        boolean isNew = submission.getId() == null;

        submission.setPosting(posting);
        submission.setUser(student);
        submission.setStatus(status);
        if (submission.getSubmittedAt() == null) {
            submission.setSubmittedAt(Instant.now().minusSeconds(3600));
        }
        submission.setSnapshotVersion(1);
        submission.setProfileSnapshotJson(profileSnapshotJson(student));
        submissionRepository.save(submission);

        String key = posting.getId() + ":" + student.getEmail();
        logCreateOrExists("submission", key, isNew);
    }

    private TaskSeedResult upsertTask(
            ApplicationPosting posting,
            UserAccount admin,
            String title,
            String description,
            LocalDate dueDate
    ) {
        ProjectTask task = projectTaskRepository.findByPostingIdAndTitleIgnoreCase(posting.getId(), title)
                .orElseGet(ProjectTask::new);
        boolean isNew = task.getId() == null;

        task.setPosting(posting);
        task.setCreatedBy(admin);
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setStatus(TaskStatus.OPEN);
        ProjectTask saved = projectTaskRepository.save(task);

        logCreateOrExists("task", posting.getId() + ":" + title, isNew);
        return new TaskSeedResult(saved, isNew);
    }

    private TaskAssignment upsertAssignment(
            ProjectTask task,
            UserAccount user,
            TaskAssignmentStatus status,
            String textAnswer,
            String reviewNote
    ) {
        TaskAssignment assignment = taskAssignmentRepository.findByTaskIdAndUserId(task.getId(), user.getId())
                .orElseGet(TaskAssignment::new);
        boolean isNew = assignment.getId() == null;

        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setAssignedAt(assignment.getAssignedAt() == null ? Instant.now().minusSeconds(7200) : assignment.getAssignedAt());
        assignment.setStatus(status);
        assignment.setUpdatedAt(Instant.now());

        if (status == TaskAssignmentStatus.ASSIGNED) {
            assignment.setTextAnswer(null);
            assignment.setSubmittedAt(null);
            assignment.setReviewedAt(null);
            assignment.setReviewNote(null);
        } else {
            assignment.setTextAnswer(textAnswer);
            assignment.setSubmittedAt(assignment.getSubmittedAt() == null ? Instant.now().minusSeconds(3600) : assignment.getSubmittedAt());
            if (status == TaskAssignmentStatus.SUBMITTED) {
                assignment.setReviewedAt(null);
                assignment.setReviewNote(null);
            } else {
                assignment.setReviewedAt(assignment.getReviewedAt() == null ? Instant.now().minusSeconds(900) : assignment.getReviewedAt());
                assignment.setReviewNote(reviewNote);
            }
        }

        TaskAssignment saved = taskAssignmentRepository.save(assignment);
        String key = task.getId() + ":" + user.getEmail();
        logCreateOrExists("assignment", key, isNew);
        return saved;
    }

    private String profileSnapshotJson(UserAccount student) {
        UserProfile profile = student.getProfile() != null
                ? student.getProfile()
                : userProfileRepository.findByUserAccountId(student.getId()).orElse(null);
        String firstName = profile != null && profile.getFirstName() != null ? profile.getFirstName() : "";
        String lastName = profile != null && profile.getLastName() != null ? profile.getLastName() : "";
        String department = profile != null && profile.getDepartment() != null ? profile.getDepartment() : "";
        String classYear = profile != null && profile.getClassYear() != null ? String.valueOf(profile.getClassYear()) : "";
        String gpa = profile != null && profile.getGpa() != null ? profile.getGpa().toPlainString() : "";
        String englishLevel = profile != null && profile.getEnglishLevel() != null ? profile.getEnglishLevel() : "";
        return """
                {"email":"%s","firstName":"%s","lastName":"%s","department":"%s","classYear":"%s","gpa":"%s","englishLevel":"%s"}
                """.formatted(
                escapeJson(student.getEmail()),
                escapeJson(firstName),
                escapeJson(lastName),
                escapeJson(department),
                escapeJson(classYear),
                escapeJson(gpa),
                escapeJson(englishLevel)
        );
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void logCreateOrExists(String entity, String key, boolean created) {
        if (created) {
            logger.info("SEED: created {} {}", entity, key);
            return;
        }
        logger.info("SEED: already exists {} {}", entity, key);
    }

    private record StudentSeed(
            String email,
            String firstName,
            String lastName,
            String department,
            int classYear,
            String gpa,
            String englishLevel
    ) {
    }

    private record PostingSeed(
            ApplicationCategory category,
            String title,
            String description,
            String projectName,
            String projectDetails
    ) {
    }

    private record TaskSeedResult(
            ProjectTask task,
            boolean created
    ) {
    }
}
