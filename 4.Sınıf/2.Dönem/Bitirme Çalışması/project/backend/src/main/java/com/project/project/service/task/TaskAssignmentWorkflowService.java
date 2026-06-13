package com.project.project.service.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.task.TaskAssignMode;
import com.project.project.dto.task.TaskAssignmentDetailResponse;
import com.project.project.dto.task.TaskAssignmentListItemResponse;
import com.project.project.dto.task.AdminTaskMutationResponse;
import com.project.project.dto.task.AdminTaskPatchRequest;
import com.project.project.dto.task.AdminTaskReviewRequest;
import com.project.project.dto.task.TaskCreateRequest;
import com.project.project.dto.task.TaskCreateResponse;
import com.project.project.dto.task.TaskFileResponse;
import com.project.project.dto.task.TaskGraphNodeStatus;
import com.project.project.dto.task.TaskScope;
import com.project.project.dto.task.TaskReviewDecision;
import com.project.project.dto.task.TaskReviewRequest;
import com.project.project.dto.task.TaskSubmissionDataRequest;
import com.project.project.dto.task.TaskUserSummaryResponse;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationSubmission;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.entity.Document;
import com.project.project.entity.ProjectTask;
import com.project.project.entity.TaskAssignment;
import com.project.project.entity.TaskAssignmentStatus;
import com.project.project.entity.TaskAttachment;
import com.project.project.entity.TaskSubmissionFile;
import com.project.project.entity.TimelineEvent;
import com.project.project.entity.TimelineEventType;
import com.project.project.entity.TimelineScopeType;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.DocumentRepository;
import com.project.project.repository.ProjectTaskRepository;
import com.project.project.repository.TaskAssignmentRepository;
import com.project.project.repository.TaskAttachmentRepository;
import com.project.project.repository.TaskSubmissionFileRepository;
import com.project.project.repository.TimelineEventRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.storage.DocumentType;
import com.project.project.service.storage.FileStorageResult;
import com.project.project.service.storage.FileStorageService;

/**
 * Task + assignment + submission + review workflow service.
 */
@Service
public class TaskAssignmentWorkflowService {

    private final ProjectTaskRepository taskRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskSubmissionFileRepository submissionFileRepository;
    private final ApplicationPostingRepository postingRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final TaskTimelineService taskTimelineService;
    private final TimelineEventRepository timelineEventRepository;

    public TaskAssignmentWorkflowService(
            ProjectTaskRepository taskRepository,
            TaskAssignmentRepository assignmentRepository,
            TaskAttachmentRepository taskAttachmentRepository,
            TaskSubmissionFileRepository submissionFileRepository,
            ApplicationPostingRepository postingRepository,
            ApplicationSubmissionRepository submissionRepository,
            UserAccountRepository userAccountRepository,
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            TaskTimelineService taskTimelineService,
            TimelineEventRepository timelineEventRepository
    ) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.postingRepository = postingRepository;
        this.submissionRepository = submissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.taskTimelineService = taskTimelineService;
        this.timelineEventRepository = timelineEventRepository;
    }

    @Transactional
    public TaskCreateResponse createTask(Long adminId, Long projectId, TaskCreateRequest request) {
        ApplicationPosting posting = postingRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        TaskAssignMode mode = request.resolvedAssignMode();
        if (mode == null) {
            throw new BadRequestException("assignMode/scope is required");
        }
        if (mode == TaskAssignMode.USER && request.resolvedAssigneeUserId() == null) {
            throw new BadRequestException("assignedToUserId is required when scope=USER");
        }
        if (mode == TaskAssignMode.ALL && request.resolvedAssigneeUserId() != null) {
            throw new BadRequestException("assignedToUserId must be null when scope=MAIN");
        }

        ProjectTask task = new ProjectTask();
        task.setPosting(posting);
        task.setCreatedBy(admin);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        ProjectTask savedTask = taskRepository.save(task);

        List<TaskAssignment> assignments = createAssignments(savedTask, mode, request.resolvedAssigneeUserId());
        List<TaskAssignmentListItemResponse> items = assignments.stream()
                .map(this::toListItem)
                .toList();
        taskTimelineService.recordTaskCreated(admin, savedTask, mode, assignments);
        return new TaskCreateResponse(
                savedTask.getId(),
                savedTask.getPosting().getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getDueDate(),
                savedTask.getCreatedAt(),
                toUserSummary(savedTask.getCreatedBy()),
                items
        );
    }

    @Transactional
    public List<TaskFileResponse> uploadTaskAttachments(Long adminId, Long taskId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BadRequestException("At least one file is required");
        }
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        List<TaskFileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            FileStorageResult storageResult = fileStorageService.storeFile(adminId, DocumentType.TASK_ATTACHMENT, file);
            Document document = createDocument(storageResult, admin);
            Document savedDocument = documentRepository.save(document);

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setDocument(savedDocument);
            TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);
            responses.add(toTaskAttachmentResponse(savedTaskAttachment(savedAttachment), null, true));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentListItemResponse> listAssignmentsByProject(Long projectId) {
        List<TaskAssignmentListItemResponse> responses = new ArrayList<>();
        for (TaskAssignment assignment : assignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(projectId)) {
            responses.add(toListItem(assignment));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentListItemResponse> listAssignmentsByUserForAdmin(Long userId) {
        List<TaskAssignmentListItemResponse> responses = new ArrayList<>();
        for (TaskAssignment assignment : assignmentRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            responses.add(toListItem(assignment));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public TaskAssignmentDetailResponse getAssignmentForAdmin(Long assignmentId) {
        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        return toDetail(assignment, true);
    }

    @Transactional
    public TaskAssignmentDetailResponse reviewAssignment(Long adminId, Long assignmentId, TaskReviewRequest request) {
        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        if (assignment.getStatus() != TaskAssignmentStatus.SUBMITTED) {
            throw new ConflictException("ASSIGNMENT_NOT_SUBMITTED");
        }
        assignment.setStatus(mapDecision(request.decision()));
        assignment.setReviewedAt(Instant.now());
        assignment.setReviewNote(request.note());
        assignment.setUpdatedAt(Instant.now());
        TaskAssignment saved = assignmentRepository.save(assignment);
        taskTimelineService.recordAssignmentReviewed(admin, saved, toTimelineReviewEvent(request.decision()));
        return toDetail(saved, true);
    }

    @Transactional
    public int backfillMainTaskAssignmentsForUser(Long projectId, Long userId) {
        assertApprovedMember(projectId, userId);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assignee user not found"));
        List<ProjectTask> tasks = taskRepository.findByPostingIdOrderByCreatedAtDesc(projectId);
        if (tasks.isEmpty()) {
            return 0;
        }

        Map<Long, TaskAssignMode> assignModes = resolveAssignModes(projectId, tasks);
        int createdCount = 0;
        for (ProjectTask task : tasks) {
            TaskAssignMode mode = assignModes.getOrDefault(task.getId(), inferFallbackAssignMode(task));
            if (mode != TaskAssignMode.ALL) {
                continue;
            }
            boolean exists = assignmentRepository.findByTaskIdAndUserId(task.getId(), userId).isPresent();
            if (exists) {
                continue;
            }
            saveAssignment(task, user);
            createdCount += 1;
        }
        return createdCount;
    }

    @Transactional
    public AdminTaskMutationResponse patchTask(Long adminId, Long taskId, AdminTaskPatchRequest request) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            task.setDescription(request.description());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        TaskScope scope = request.scope();
        TaskScope resolvedScope = null;
        if (scope != null || request.assignedToUserId() != null) {
            resolvedScope = scope != null ? scope : inferScope(task);
            syncAssignmentsForScope(task, resolvedScope, request.assignedToUserId());
        }
        if (request.status() != null) {
            applyTaskStatus(task, request.status(), request.assignedToUserId(), admin, null);
        }
        if (resolvedScope != null) {
            List<TaskAssignment> taskAssignments = assignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(task.getPosting().getId())
                    .stream()
                    .filter(assignment -> assignment.getTask().getId().equals(task.getId()))
                    .toList();
            TaskAssignMode assignMode = resolvedScope == TaskScope.MAIN ? TaskAssignMode.ALL : TaskAssignMode.USER;
            taskTimelineService.recordTaskScopeUpdated(admin, task, assignMode, taskAssignments);
        }
        taskRepository.save(task);
        return buildMutationResponse(task, request.assignedToUserId());
    }

    @Transactional
    public void deleteTask(Long taskId) {
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        timelineEventRepository.deleteByTaskId(task.getId());
        taskRepository.delete(task);
    }

    @Transactional
    public AdminTaskMutationResponse reviewTask(Long adminId, Long taskId, AdminTaskReviewRequest request) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        applyTaskStatus(task, request.status(), request.assignedToUserId(), admin, request.reviewNote());
        taskRepository.save(task);
        return buildMutationResponse(task, request.assignedToUserId());
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentListItemResponse> listMyAssignments(Long userId) {
        List<TaskAssignmentListItemResponse> responses = new ArrayList<>();
        for (TaskAssignment assignment : assignmentRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            responses.add(toListItem(assignment));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public TaskAssignmentDetailResponse getMyAssignment(Long userId, Long assignmentId) {
        TaskAssignment assignment = assignmentRepository.findByIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        return toDetail(assignment, false);
    }

    @Transactional
    public TaskAssignmentDetailResponse submitAssignment(
            Long userId,
            Long assignmentId,
            TaskSubmissionDataRequest data,
            MultipartFile[] files
    ) {
        TaskAssignment assignment = assignmentRepository.findByIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        if (assignment.getStatus() != TaskAssignmentStatus.ASSIGNED
                && assignment.getStatus() != TaskAssignmentStatus.REVISION_REQUESTED) {
            throw new ConflictException("ASSIGNMENT_NOT_SUBMITTABLE");
        }
        assignment.setTextAnswer(data != null ? data.textAnswer() : null);
        assignment.setStatus(TaskAssignmentStatus.SUBMITTED);
        assignment.setSubmittedAt(Instant.now());
        assignment.setUpdatedAt(Instant.now());
        assignment.setReviewNote(null);
        assignment.setReviewedAt(null);
        TaskAssignment savedAssignment = assignmentRepository.save(assignment);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        taskTimelineService.recordAssignmentSubmitted(user, savedAssignment);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                FileStorageResult storageResult = fileStorageService.storeFile(userId, DocumentType.TASK_SUBMISSION, file);
                Document document = createDocument(storageResult, user);
                Document savedDocument = documentRepository.save(document);
                TaskSubmissionFile submissionFile = new TaskSubmissionFile();
                submissionFile.setAssignment(savedAssignment);
                submissionFile.setDocument(savedDocument);
                submissionFileRepository.save(submissionFile);
            }
        }
        return toDetail(savedAssignment, false);
    }

    @Transactional(readOnly = true)
    public FileDownload downloadTaskAttachmentForAdmin(Long taskId, Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new NotFoundException("Task attachment not found"));
        return toDownload(attachment.getDocument());
    }

    @Transactional(readOnly = true)
    public FileDownload downloadTaskAttachmentForStudent(Long userId, Long assignmentId, Long attachmentId) {
        TaskAssignment assignment = assignmentRepository.findByIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        TaskAttachment attachment = taskAttachmentRepository.findByIdAndTaskId(attachmentId, assignment.getTask().getId())
                .orElseThrow(() -> new NotFoundException("Task attachment not found"));
        return toDownload(attachment.getDocument());
    }

    @Transactional(readOnly = true)
    public FileDownload downloadSubmissionFileForAdmin(Long assignmentId, Long fileId) {
        TaskSubmissionFile submissionFile = submissionFileRepository.findByIdAndAssignmentId(fileId, assignmentId)
                .orElseThrow(() -> new NotFoundException("Submission file not found"));
        return toDownload(submissionFile.getDocument());
    }

    @Transactional(readOnly = true)
    public FileDownload downloadSubmissionFileForStudent(Long userId, Long assignmentId, Long fileId) {
        TaskAssignment assignment = assignmentRepository.findByIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        TaskSubmissionFile submissionFile = submissionFileRepository.findByIdAndAssignmentId(fileId, assignment.getId())
                .orElseThrow(() -> new NotFoundException("Submission file not found"));
        return toDownload(submissionFile.getDocument());
    }

    private List<TaskAssignment> createAssignments(ProjectTask task, TaskAssignMode mode, Long assigneeUserId) {
        if (mode == TaskAssignMode.USER) {
            if (assigneeUserId == null) {
                throw new BadRequestException("assignedToUserId is required when scope=USER");
            }
            UserAccount assignee = userAccountRepository.findById(assigneeUserId)
                    .orElseThrow(() -> new NotFoundException("Assignee user not found"));
            assertApprovedMember(task.getPosting().getId(), assignee.getId());
            return List.of(saveAssignment(task, assignee));
        }
        if (mode == TaskAssignMode.ALL) {
            List<ApplicationSubmission> submissions = submissionRepository.findByPostingIdAndStatusAndUserEnabledTrue(
                    task.getPosting().getId(),
                    ApplicationSubmissionStatus.APPROVED
            );
            Map<Long, UserAccount> uniqueUsers = new LinkedHashMap<>();
            for (ApplicationSubmission submission : submissions) {
                uniqueUsers.put(submission.getUser().getId(), submission.getUser());
            }
            if (uniqueUsers.isEmpty()) {
                throw new BadRequestException("No approved users found for project");
            }
            List<TaskAssignment> assignments = new ArrayList<>();
            for (UserAccount user : uniqueUsers.values()) {
                assignments.add(saveAssignment(task, user));
            }
            return assignments;
        }
        throw new BadRequestException("Invalid assignMode");
    }

    private Map<Long, TaskAssignMode> resolveAssignModes(Long projectId, List<ProjectTask> tasks) {
        Map<Long, TaskAssignMode> modes = new LinkedHashMap<>();
        Set<Long> taskIds = new HashSet<>();
        for (ProjectTask task : tasks) {
            taskIds.add(task.getId());
        }
        List<TimelineEvent> events = timelineEventRepository.findByScopeTypeAndScopeIdOrderByCreatedAtAscIdAsc(
                TimelineScopeType.PROJECT,
                projectId
        );
        for (TimelineEvent event : events) {
            if (event.getTask() == null || !taskIds.contains(event.getTask().getId())) {
                continue;
            }
            if (event.getEventType() == TimelineEventType.TASK_ASSIGNED_ALL) {
                modes.put(event.getTask().getId(), TaskAssignMode.ALL);
            } else if (event.getEventType() == TimelineEventType.TASK_ASSIGNED_USER) {
                modes.put(event.getTask().getId(), TaskAssignMode.USER);
            }
        }
        return modes;
    }

    private TaskAssignMode inferFallbackAssignMode(ProjectTask task) {
        if (task.getAssignments() == null || task.getAssignments().isEmpty()) {
            return TaskAssignMode.ALL;
        }
        return task.getAssignments().size() > 1 ? TaskAssignMode.ALL : TaskAssignMode.USER;
    }

    private void syncAssignmentsForScope(ProjectTask task, TaskScope scope, Long assignedToUserId) {
        List<TaskAssignment> current = assignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(task.getPosting().getId())
                .stream()
                .filter(assignment -> assignment.getTask().getId().equals(task.getId()))
                .toList();
        if (scope == TaskScope.USER) {
            if (assignedToUserId == null) {
                throw new BadRequestException("assignedToUserId is required when scope=USER");
            }
            UserAccount assignee = userAccountRepository.findById(assignedToUserId)
                    .orElseThrow(() -> new NotFoundException("Assignee user not found"));
            assertApprovedMember(task.getPosting().getId(), assignee.getId());
            for (TaskAssignment assignment : current) {
                if (!assignment.getUser().getId().equals(assignee.getId())) {
                    assignmentRepository.delete(assignment);
                }
            }
            assignmentRepository.findByTaskIdAndUserId(task.getId(), assignee.getId())
                    .orElseGet(() -> saveAssignment(task, assignee));
            return;
        }

        if (assignedToUserId != null) {
            throw new BadRequestException("assignedToUserId must be null when scope=MAIN");
        }
        List<ApplicationSubmission> submissions = submissionRepository.findByPostingIdAndStatusAndUserEnabledTrue(
                task.getPosting().getId(),
                ApplicationSubmissionStatus.APPROVED
        );
        Map<Long, UserAccount> approvedUsers = new LinkedHashMap<>();
        for (ApplicationSubmission submission : submissions) {
            approvedUsers.put(submission.getUser().getId(), submission.getUser());
        }
        if (approvedUsers.isEmpty()) {
            throw new BadRequestException("No approved users found for project");
        }
        for (TaskAssignment assignment : current) {
            if (!approvedUsers.containsKey(assignment.getUser().getId())) {
                assignmentRepository.delete(assignment);
            }
        }
        for (UserAccount user : approvedUsers.values()) {
            assignmentRepository.findByTaskIdAndUserId(task.getId(), user.getId())
                    .orElseGet(() -> saveAssignment(task, user));
        }
    }

    private TaskScope inferScope(ProjectTask task) {
        List<TaskAssignment> assignments = assignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(task.getPosting().getId())
                .stream()
                .filter(assignment -> assignment.getTask().getId().equals(task.getId()))
                .toList();
        return assignments.size() <= 1 ? TaskScope.USER : TaskScope.MAIN;
    }

    private void applyTaskStatus(
            ProjectTask task,
            TaskGraphNodeStatus targetStatus,
            Long assignedToUserId,
            UserAccount admin,
            String reviewNote
    ) {
        List<TaskAssignment> assignments = assignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(task.getPosting().getId())
                .stream()
                .filter(assignment -> assignment.getTask().getId().equals(task.getId()))
                .toList();
        if (assignments.isEmpty()) {
            throw new NotFoundException("Task assignment not found");
        }
        List<TaskAssignment> targets;
        if (assignedToUserId != null) {
            targets = assignments.stream()
                    .filter(assignment -> assignment.getUser().getId().equals(assignedToUserId))
                    .toList();
            if (targets.isEmpty()) {
                throw new NotFoundException("Task assignment not found for user");
            }
        } else if (assignments.size() == 1) {
            targets = assignments;
        } else {
            throw new BadRequestException("assignedToUserId is required for MAIN task status update");
        }

        for (TaskAssignment assignment : targets) {
            assignment.setStatus(mapGraphStatus(targetStatus));
            assignment.setUpdatedAt(Instant.now());
            if (targetStatus != TaskGraphNodeStatus.PENDING) {
                assignment.setSubmittedAt(assignment.getSubmittedAt() == null ? Instant.now() : assignment.getSubmittedAt());
                assignment.setReviewedAt(Instant.now());
                assignment.setReviewNote(reviewNote);
                taskTimelineService.recordAssignmentReviewed(admin, assignment, mapGraphTimelineEvent(targetStatus));
            } else {
                assignment.setReviewedAt(null);
                assignment.setReviewNote(null);
            }
            assignmentRepository.save(assignment);
        }
    }

    private TaskAssignmentStatus mapGraphStatus(TaskGraphNodeStatus status) {
        return switch (status) {
            case SUCCESS -> TaskAssignmentStatus.APPROVED;
            case FAILED -> TaskAssignmentStatus.REJECTED;
            case REVISION_REQUESTED -> TaskAssignmentStatus.REVISION_REQUESTED;
            case SUBMITTED -> TaskAssignmentStatus.SUBMITTED;
            case PENDING -> TaskAssignmentStatus.ASSIGNED;
        };
    }

    private TimelineEventType mapGraphTimelineEvent(TaskGraphNodeStatus status) {
        return switch (status) {
            case SUCCESS -> TimelineEventType.TASK_REVIEWED_APPROVED;
            case FAILED -> TimelineEventType.TASK_REVIEWED_REJECTED;
            case REVISION_REQUESTED -> TimelineEventType.TASK_REVIEWED_REVISION;
            case SUBMITTED -> TimelineEventType.TASK_SUBMITTED;
            case PENDING -> TimelineEventType.TASK_SUBMITTED;
        };
    }

    private AdminTaskMutationResponse buildMutationResponse(ProjectTask task, Long assignedToUserId) {
        List<TaskAssignment> assignments = assignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(task.getPosting().getId())
                .stream()
                .filter(assignment -> assignment.getTask().getId().equals(task.getId()))
                .toList();
        Long resolvedAssignedTo = assignedToUserId;
        if (resolvedAssignedTo == null && assignments.size() == 1) {
            resolvedAssignedTo = assignments.get(0).getUser().getId();
        }
        final Long finalAssignedTo = resolvedAssignedTo;
        String branchKey = resolvedAssignedTo == null ? "MAIN" : "USER-" + resolvedAssignedTo;
        TaskGraphNodeStatus status = resolvedAssignedTo == null
                ? aggregateGraphStatus(assignments)
                : assignments.stream()
                .filter(assignment -> assignment.getUser().getId().equals(finalAssignedTo))
                .findFirst()
                .map(assignment -> mapGraphStatusFromAssignment(assignment.getStatus()))
                .orElse(TaskGraphNodeStatus.PENDING);
        return new AdminTaskMutationResponse(
                task.getId(),
                task.getPosting().getId(),
                branchKey,
                status,
                resolvedAssignedTo,
                Instant.now()
        );
    }

    private TaskGraphNodeStatus aggregateGraphStatus(List<TaskAssignment> assignments) {
        if (assignments.isEmpty()) {
            return TaskGraphNodeStatus.PENDING;
        }
        boolean allSuccess = assignments.stream().allMatch(assignment ->
                assignment.getStatus() == TaskAssignmentStatus.APPROVED || assignment.getStatus() == TaskAssignmentStatus.DONE);
        if (allSuccess) {
            return TaskGraphNodeStatus.SUCCESS;
        }
        if (assignments.stream().anyMatch(assignment -> assignment.getStatus() == TaskAssignmentStatus.REVISION_REQUESTED)) {
            return TaskGraphNodeStatus.REVISION_REQUESTED;
        }
        if (assignments.stream().anyMatch(assignment ->
                assignment.getStatus() == TaskAssignmentStatus.REJECTED || assignment.getStatus() == TaskAssignmentStatus.FAILED)) {
            return TaskGraphNodeStatus.FAILED;
        }
        return TaskGraphNodeStatus.PENDING;
    }

    private TaskGraphNodeStatus mapGraphStatusFromAssignment(TaskAssignmentStatus status) {
        return switch (status) {
            case APPROVED, DONE -> TaskGraphNodeStatus.SUCCESS;
            case REJECTED, FAILED -> TaskGraphNodeStatus.FAILED;
            case REVISION_REQUESTED -> TaskGraphNodeStatus.REVISION_REQUESTED;
            case SUBMITTED -> TaskGraphNodeStatus.SUBMITTED;
            default -> TaskGraphNodeStatus.PENDING;
        };
    }

    private TaskAssignment saveAssignment(ProjectTask task, UserAccount assignee) {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setUser(assignee);
        assignment.setStatus(TaskAssignmentStatus.ASSIGNED);
        assignment.setAssignedAt(Instant.now());
        assignment.setUpdatedAt(Instant.now());
        return assignmentRepository.save(assignment);
    }

    private void assertApprovedMember(Long postingId, Long userId) {
        boolean approved = submissionRepository.existsByPostingIdAndUserIdAndStatusAndUserEnabledTrue(
                postingId,
                userId,
                ApplicationSubmissionStatus.APPROVED
        );
        if (!approved) {
            throw new ConflictException("USER_NOT_APPROVED_MEMBER");
        }
    }

    private TaskAssignmentStatus mapDecision(TaskReviewDecision decision) {
        return switch (decision) {
            case APPROVED -> TaskAssignmentStatus.APPROVED;
            case REJECTED -> TaskAssignmentStatus.REJECTED;
            case REVISION_REQUESTED -> TaskAssignmentStatus.REVISION_REQUESTED;
        };
    }

    private TimelineEventType toTimelineReviewEvent(TaskReviewDecision decision) {
        return switch (decision) {
            case APPROVED -> TimelineEventType.TASK_REVIEWED_APPROVED;
            case REJECTED -> TimelineEventType.TASK_REVIEWED_REJECTED;
            case REVISION_REQUESTED -> TimelineEventType.TASK_REVIEWED_REVISION;
        };
    }

    private Document createDocument(FileStorageResult result, UserAccount uploadedBy) {
        Document document = new Document();
        document.setOriginalFileName(result.originalFileName());
        document.setContentType(result.contentType());
        document.setSize(result.size());
        document.setStorageKey(result.storageKey());
        document.setChecksum(result.checksum());
        document.setUploadedAt(Instant.now());
        document.setUploadedBy(uploadedBy);
        return document;
    }

    private TaskAssignmentListItemResponse toListItem(TaskAssignment assignment) {
        return new TaskAssignmentListItemResponse(
                assignment.getId(),
                assignment.getTask().getId(),
                assignment.getTask().getPosting().getId(),
                assignment.getTask().getTitle(),
                assignment.getTask().getDueDate(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getSubmittedAt(),
                assignment.getReviewedAt(),
                toUserSummary(assignment.getUser())
        );
    }

    private TaskAssignmentDetailResponse toDetail(TaskAssignment assignment, boolean adminView) {
        List<TaskFileResponse> taskAttachments = taskAttachmentRepository.findByTaskIdOrderByCreatedAtAsc(
                assignment.getTask().getId()).stream()
                .map(attachment -> toTaskAttachmentResponse(attachment, assignment.getId(), adminView))
                .toList();
        List<TaskFileResponse> submissionFiles = submissionFileRepository.findByAssignmentIdOrderByCreatedAtAsc(
                assignment.getId()).stream()
                .map(file -> toSubmissionFileResponse(file, adminView))
                .toList();
        return new TaskAssignmentDetailResponse(
                assignment.getId(),
                assignment.getTask().getId(),
                assignment.getTask().getPosting().getId(),
                assignment.getTask().getTitle(),
                assignment.getTask().getDescription(),
                assignment.getTask().getDueDate(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getSubmittedAt(),
                assignment.getReviewedAt(),
                assignment.getReviewNote(),
                assignment.getTextAnswer(),
                toUserSummary(assignment.getUser()),
                toUserSummary(assignment.getTask().getCreatedBy()),
                taskAttachments,
                submissionFiles
        );
    }

    private TaskFileResponse toTaskAttachmentResponse(TaskAttachment attachment, Long assignmentId, boolean adminView) {
        Document document = attachment.getDocument();
        String downloadUrl = adminView
                ? "/api/v1/admin/tasks/" + attachment.getTask().getId()
                + "/attachments/" + attachment.getId() + "/download"
                : "/api/v1/me/task-assignments/" + assignmentId
                + "/task-attachments/" + attachment.getId() + "/download";
        return new TaskFileResponse(
                attachment.getId(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSize(),
                document.getUploadedAt(),
                downloadUrl
        );
    }

    private TaskFileResponse toSubmissionFileResponse(TaskSubmissionFile submissionFile, boolean adminView) {
        Document document = submissionFile.getDocument();
        String downloadUrl = adminView
                ? "/api/v1/admin/task-assignments/" + submissionFile.getAssignment().getId()
                + "/submission-files/" + submissionFile.getId() + "/download"
                : "/api/v1/me/task-assignments/" + submissionFile.getAssignment().getId()
                + "/submission-files/" + submissionFile.getId() + "/download";
        return new TaskFileResponse(
                submissionFile.getId(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSize(),
                document.getUploadedAt(),
                downloadUrl
        );
    }

    private TaskUserSummaryResponse toUserSummary(UserAccount user) {
        String firstName = user.getProfile() != null ? user.getProfile().getFirstName() : null;
        String lastName = user.getProfile() != null ? user.getProfile().getLastName() : null;
        return new TaskUserSummaryResponse(
                user.getId(),
                user.getEmail(),
                firstName,
                lastName
        );
    }

    private TaskAttachment savedTaskAttachment(TaskAttachment attachment) {
        return taskAttachmentRepository.findByIdAndTaskId(attachment.getId(), attachment.getTask().getId())
                .orElse(attachment);
    }

    private FileDownload toDownload(Document document) {
        Resource resource = fileStorageService.loadFileAsResource(document.getStorageKey());
        return new FileDownload(resource, document.getContentType(), document.getOriginalFileName());
    }

    /**
     * File download payload.
     */
    public record FileDownload(Resource resource, String contentType, String originalFileName) {
    }
}
