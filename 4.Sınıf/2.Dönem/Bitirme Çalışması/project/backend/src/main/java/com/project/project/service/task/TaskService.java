package com.project.project.service.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.task.TaskAssignmentResponse;
import com.project.project.dto.task.TaskAssignmentUpdateRequest;
import com.project.project.dto.task.TaskResponse;
import com.project.project.dto.task.TaskUpsertRequest;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.entity.ProjectTask;
import com.project.project.entity.TaskAssignment;
import com.project.project.entity.TaskAssignmentStatus;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.ProjectTaskRepository;
import com.project.project.repository.TaskAssignmentRepository;
import com.project.project.repository.TimelineEventRepository;
import com.project.project.repository.UserAccountRepository;

/**
 * Admin and user task management flows.
 */
@Service
public class TaskService {

    private final ProjectTaskRepository taskRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final ApplicationPostingRepository postingRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final TimelineEventRepository timelineEventRepository;

    public TaskService(
            ProjectTaskRepository taskRepository,
            TaskAssignmentRepository assignmentRepository,
            ApplicationPostingRepository postingRepository,
            UserAccountRepository userAccountRepository,
            ApplicationSubmissionRepository submissionRepository,
            TimelineEventRepository timelineEventRepository) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.postingRepository = postingRepository;
        this.userAccountRepository = userAccountRepository;
        this.submissionRepository = submissionRepository;
        this.timelineEventRepository = timelineEventRepository;
    }

    @Transactional
    public TaskResponse create(Long adminId, Long postingId, TaskUpsertRequest request) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        ProjectTask task = new ProjectTask();
        task.setPosting(posting);
        task.setCreatedBy(admin);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listByPosting(Long postingId) {
        List<TaskResponse> responses = new ArrayList<>();
        for (ProjectTask task : taskRepository.findByPostingIdOrderByCreatedAtDesc(postingId)) {
            responses.add(toResponse(task));
        }
        return responses;
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskUpsertRequest request) {
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Task not found");
        }
        timelineEventRepository.deleteByTaskId(taskId);
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public TaskResponse assign(Long taskId, Long userId, TaskAssignmentUpdateRequest request) {
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        boolean isMember = submissionRepository.existsByPostingIdAndUserIdAndStatusAndUserEnabledTrue(
                task.getPosting().getId(),
                userId,
                ApplicationSubmissionStatus.APPROVED);
        if (!isMember) {
            throw new ConflictException("USER_NOT_APPROVED_MEMBER");
        }
        TaskAssignment assignment = assignmentRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseGet(TaskAssignment::new);
        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setStatus(TaskAssignmentStatus.ASSIGNED);
        assignment.setNote(request != null ? request.note() : null);
        if (assignment.getAssignedAt() == null) {
            assignment.setAssignedAt(Instant.now());
        }
        assignment.setUpdatedAt(Instant.now());
        assignmentRepository.save(assignment);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse markDone(Long taskId, Long userId, TaskAssignmentUpdateRequest request) {
        TaskAssignment assignment = assignmentRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("Task assignment not found"));
        assignment.setStatus(TaskAssignmentStatus.DONE);
        assignment.setNote(request != null ? request.note() : assignment.getNote());
        assignment.setUpdatedAt(Instant.now());
        assignmentRepository.save(assignment);
        return toResponse(assignment.getTask());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listMine(Long userId, Long postingId) {
        List<TaskResponse> responses = new ArrayList<>();
        List<TaskAssignment> assignments = postingId == null
                ? assignmentRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                : assignmentRepository.findByUserIdAndTaskPostingIdOrderByUpdatedAtDesc(userId, postingId);
        for (TaskAssignment assignment : assignments) {
            responses.add(toResponse(assignment.getTask()));
        }
        return responses;
    }

    private TaskResponse toResponse(ProjectTask task) {
        List<TaskAssignmentResponse> assignments = new ArrayList<>();
        for (TaskAssignment assignment : task.getAssignments()) {
            assignments.add(new TaskAssignmentResponse(
                    assignment.getUser().getId(),
                    assignment.getStatus(),
                    assignment.getNote(),
                    assignment.getUpdatedAt()));
        }
        return new TaskResponse(
                task.getId(),
                task.getPosting().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                task.getCreatedAt(),
                assignments);
    }
}
