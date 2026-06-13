package com.project.project.service.submission;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.submission.AdminSubmissionListItemResponse;
import com.project.project.dto.submission.AdminSubmissionDetailResponse;
import com.project.project.dto.submission.SubmissionListResponse;
import com.project.project.dto.submission.SubmissionResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.entity.ApplicationSubmission;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.projection.AdminSubmissionRowProjection;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.policy.PostingVisibilityPolicy;
import com.project.project.service.task.TaskAssignmentWorkflowService;

/**
 * Handles create/list/detail flows for posting submissions.
 */
@Service
public class ApplicationSubmissionService {

    private final ApplicationSubmissionRepository submissionRepository;
    private final ApplicationPostingRepository postingRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostingVisibilityPolicy postingVisibilityPolicy;
    private final ObjectMapper objectMapper;
    private final TaskAssignmentWorkflowService taskAssignmentWorkflowService;

    public ApplicationSubmissionService(
            ApplicationSubmissionRepository submissionRepository,
            ApplicationPostingRepository postingRepository,
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            PostingVisibilityPolicy postingVisibilityPolicy,
            ObjectMapper objectMapper,
            TaskAssignmentWorkflowService taskAssignmentWorkflowService) {
        this.submissionRepository = submissionRepository;
        this.postingRepository = postingRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.postingVisibilityPolicy = postingVisibilityPolicy;
        this.objectMapper = objectMapper;
        this.taskAssignmentWorkflowService = taskAssignmentWorkflowService;
    }

    @Transactional
    public SubmissionResponse create(Long userId, Long postingId) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        postingVisibilityPolicy.assertSubmissionAllowed(posting);
        if (submissionRepository.existsByPostingIdAndUserId(postingId, userId)) {
            throw new ConflictException("ALREADY_SUBMITTED");
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserProfile profile = userProfileRepository.findByUserAccountId(userId)
                .orElse(null);

        ApplicationSubmission submission = new ApplicationSubmission();
        submission.setPosting(posting);
        submission.setUser(user);
        submission.setSubmittedAt(Instant.now());
        submission.setProfileSnapshotJson(buildSnapshotJson(user, profile));
        submission.setSnapshotVersion(1);
        submission.setCvDocumentIdSnapshot(profile != null && profile.getCvDocument() != null
                ? profile.getCvDocument().getId()
                : null);
        try {
            return toResponse(submissionRepository.save(submission));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("ALREADY_SUBMITTED");
        }
    }

    @Transactional
    public SubmissionResponse accept(Long submissionId) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        submission.setStatus(ApplicationSubmissionStatus.APPROVED);
        ApplicationSubmission saved = submissionRepository.save(submission);
        taskAssignmentWorkflowService.backfillMainTaskAssignmentsForUser(
                saved.getPosting().getId(),
                saved.getUser().getId()
        );
        return toResponse(saved);
    }

    @Transactional
    public SubmissionResponse addOrUpdateByAdmin(Long postingId, Long userId, ApplicationSubmissionStatus requestedStatus) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() != Role.USER) {
            throw new BadRequestException("TARGET_ACCOUNT_MUST_BE_USER");
        }

        ApplicationSubmissionStatus status = requestedStatus != null ? requestedStatus : ApplicationSubmissionStatus.APPROVED;
        if (status == ApplicationSubmissionStatus.CANCELED || status == ApplicationSubmissionStatus.REMOVED) {
            throw new BadRequestException("MANUAL_STATUS_NOT_ALLOWED");
        }

        ApplicationSubmission existingSubmission = submissionRepository.findByPostingIdAndUserId(postingId, userId)
                .orElse(null);
        if (existingSubmission != null
                && safeStatus(existingSubmission.getStatus()) == ApplicationSubmissionStatus.APPROVED
                && status == ApplicationSubmissionStatus.APPROVED) {
            throw new ConflictException("USER_ALREADY_IN_PROJECT");
        }

        ApplicationSubmission submission = existingSubmission != null ? existingSubmission : new ApplicationSubmission();
        boolean isNew = submission.getId() == null;
        submission.setPosting(posting);
        submission.setUser(user);
        submission.setStatus(status);
        if (isNew || submission.getSubmittedAt() == null) {
            submission.setSubmittedAt(Instant.now());
        }
        submission.setProfileSnapshotJson(buildSnapshotJson(user, userProfileRepository.findByUserAccountId(userId).orElse(null)));
        submission.setSnapshotVersion(1);
        submission.setCvDocumentIdSnapshot(
                user.getProfile() != null && user.getProfile().getCvDocument() != null
                        ? user.getProfile().getCvDocument().getId()
                        : null
        );

        ApplicationSubmission saved = submissionRepository.save(submission);
        if (status == ApplicationSubmissionStatus.APPROVED) {
            taskAssignmentWorkflowService.backfillMainTaskAssignmentsForUser(postingId, userId);
        }
        return toResponse(saved);
    }

    @Transactional
    public SubmissionResponse reject(Long submissionId) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        submission.setStatus(ApplicationSubmissionStatus.REJECTED);
        return toResponse(submissionRepository.save(submission));
    }

    @Transactional
    public SubmissionResponse remove(Long submissionId) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        if (submission.getStatus() != ApplicationSubmissionStatus.APPROVED) {
            throw new BadRequestException("ONLY_APPROVED_CAN_BE_REMOVED");
        }
        submission.setStatus(ApplicationSubmissionStatus.REMOVED);
        return toResponse(submissionRepository.save(submission));
    }

    @Transactional
    public SubmissionResponse deleteByAdmin(Long submissionId) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        if (safeStatus(submission.getStatus()) == ApplicationSubmissionStatus.APPROVED) {
            submission.setStatus(ApplicationSubmissionStatus.REMOVED);
        } else {
            submission.setStatus(ApplicationSubmissionStatus.CANCELED);
        }
        return toResponse(submissionRepository.save(submission));
    }

    @Transactional(readOnly = true)
    public boolean isAcceptedMember(Long postingId, Long userId) {
        return submissionRepository.existsByPostingIdAndUserIdAndStatusAndUserEnabledTrue(
                postingId, userId, ApplicationSubmissionStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public boolean hasSubmission(Long postingId, Long userId) {
        return submissionRepository.existsByPostingIdAndUserId(postingId, userId);
    }

    @Transactional(readOnly = true)
    public SubmissionListResponse listMine(Long userId) {
        List<SubmissionResponse> responses = new ArrayList<>();
        for (ApplicationSubmission submission : submissionRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            responses.add(toResponse(submission));
        }
        return new SubmissionListResponse(responses);
    }

    @Transactional(readOnly = true)
    public SubmissionListResponse listByPosting(Long postingId) {
        if (!postingRepository.existsById(postingId)) {
            throw new NotFoundException("Posting not found");
        }
        List<SubmissionResponse> responses = new ArrayList<>();
        for (ApplicationSubmission submission : submissionRepository.findByPostingIdOrderByCreatedAtDesc(postingId)) {
            responses.add(toResponse(submission));
        }
        return new SubmissionListResponse(responses);
    }

    @Transactional(readOnly = true)
    public Page<AdminSubmissionListItemResponse> listForAdmin(
            ApplicationCategory category,
            ApplicationSubmissionStatus submissionStatus,
            ApplicationPostingStatus postingStatus,
            Long postingId,
            Pageable pageable) {
        Page<AdminSubmissionRowProjection> rows = submissionRepository.findAdminSubmissionRows(
                category,
                submissionStatus,
                postingStatus,
                postingId,
                pageable);
        return rows.map(this::toAdminListItem);
    }

    @Transactional(readOnly = true)
    public AdminSubmissionDetailResponse adminDetail(Long submissionId) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        JsonNode snapshot = parseSnapshot(submission.getProfileSnapshotJson());
        String cvDownloadUrl = submission.getCvDocumentIdSnapshot() != null
                ? "/admin/users/" + submission.getUser().getId() + "/documents/"
                        + submission.getCvDocumentIdSnapshot() + "/download"
                : null;
        return new AdminSubmissionDetailResponse(
                submission.getId(),
                submission.getPosting().getId(),
                submission.getPosting().getTitle(),
                submission.getPosting().getCategory(),
                submission.getPosting().getStatus(),
                idAsString(submission.getUser().getId()),
                submission.getUser().getEmail(),
                firstNonBlank(
                        readText(snapshot, "firstName", "name"),
                        submission.getUser().getProfile() != null ? submission.getUser().getProfile().getFirstName() : null,
                        ""),
                firstNonBlank(
                        readText(snapshot, "lastName", "surname"),
                        submission.getUser().getProfile() != null ? submission.getUser().getProfile().getLastName() : null,
                        ""),
                submission.getSubmittedAt(),
                safeStatus(submission.getStatus()),
                submission.getProfileSnapshotJson(),
                submission.getSnapshotVersion(),
                submission.getCvDocumentIdSnapshot(),
                cvDownloadUrl);
    }

    private String buildSnapshotJson(UserAccount user, UserProfile profile) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("userId", user.getId());
        snapshot.put("email", user.getEmail());
        if (profile != null) {
            snapshot.put("firstName", profile.getFirstName());
            snapshot.put("lastName", profile.getLastName());
            snapshot.put("classYear", profile.getClassYear());
            snapshot.put("department", profile.getDepartment());
            snapshot.put("englishLevel", profile.getEnglishLevel());
            snapshot.put("gpa", profile.getGpa());
            snapshot.put("phoneNumber", profile.getPhoneNumber());
            snapshot.put("dateOfBirth", profile.getDateOfBirth());
            snapshot.put("cvDocumentId", profile.getCvDocument() != null ? profile.getCvDocument().getId() : null);
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize snapshot");
        }
    }

    private SubmissionResponse toResponse(ApplicationSubmission submission) {
        return new SubmissionResponse(
                idAsString(submission.getId()),
                idAsString(submission.getPosting().getId()),
                submission.getPosting().getTitle(),
                submission.getPosting().getCategory(),
                idAsString(submission.getUser().getId()),
                submission.getSubmittedAt(),
                safeStatus(submission.getStatus()));
    }

    private AdminSubmissionListItemResponse toAdminListItem(AdminSubmissionRowProjection row) {
        JsonNode snapshot = parseSnapshot(row.getProfileSnapshotJson());
        String cvDownloadUrl = row.getCvDocumentIdSnapshot() != null
                ? "/admin/users/" + row.getUserId() + "/documents/" + row.getCvDocumentIdSnapshot() + "/download"
                : null;
        String resolvedFirstName = firstNonBlank(
                readText(snapshot, "firstName", "name"),
                row.getProfileFirstName(),
                "");
        String resolvedLastName = firstNonBlank(
                readText(snapshot, "lastName", "surname"),
                row.getProfileLastName(),
                "");
        return new AdminSubmissionListItemResponse(
                row.getSubmissionId(),
                row.getPostingId(),
                row.getPostingTitle(),
                row.getPostingCategory(),
                row.getPostingStatus(),
                safeStatus(row.getSubmissionStatus()),
                row.getSubmittedAt(),
                idAsString(row.getUserId()),
                row.getUserEmail(),
                resolvedFirstName,
                resolvedLastName,
                readInt(snapshot, "classYear"),
                readText(snapshot, "department"),
                readText(snapshot, "englishLevel"),
                readDecimal(snapshot, "gpa"),
                cvDownloadUrl);
    }

    private JsonNode parseSnapshot(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            return objectMapper.createObjectNode();
        }
    }

    private String readText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull()) {
                return value.asText();
            }
        }
        return null;
    }

    private Integer readInt(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private java.math.BigDecimal readDecimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(value.asText());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String idAsString(Long id) {
        return id != null ? String.valueOf(id) : "";
    }

    private ApplicationSubmissionStatus safeStatus(ApplicationSubmissionStatus status) {
        return status != null ? status : ApplicationSubmissionStatus.PENDING;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
