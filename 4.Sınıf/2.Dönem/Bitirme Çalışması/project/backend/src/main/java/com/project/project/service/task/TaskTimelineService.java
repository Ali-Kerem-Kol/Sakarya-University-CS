package com.project.project.service.task;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.dto.task.TaskAssignMode;
import com.project.project.dto.task.TaskTimelineActorResponse;
import com.project.project.dto.task.TaskTimelineAssignmentResponse;
import com.project.project.dto.task.TaskTimelineEventResponse;
import com.project.project.dto.task.TaskTimelineStatsResponse;
import com.project.project.dto.task.TaskTimelineTaskResponse;
import com.project.project.entity.ProjectTask;
import com.project.project.entity.TaskAssignment;
import com.project.project.entity.TaskAssignmentStatus;
import com.project.project.entity.TimelineEvent;
import com.project.project.entity.TimelineEventType;
import com.project.project.entity.TimelineScopeType;
import com.project.project.entity.UserAccount;
import com.project.project.repository.TaskAssignmentRepository;
import com.project.project.repository.TimelineEventRepository;

/**
 * Emits and queries task timeline events.
 */
@Service
public class TaskTimelineService {

    private final TimelineEventRepository timelineEventRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ObjectMapper objectMapper;

    public TaskTimelineService(
            TimelineEventRepository timelineEventRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            ObjectMapper objectMapper
    ) {
        this.timelineEventRepository = timelineEventRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordTaskCreated(UserAccount actor, ProjectTask task, TaskAssignMode assignMode, List<TaskAssignment> assignments) {
        TaskTimelineStatsResponse stats = buildProjectStats(task.getPosting().getId());
        TimelineEventType assignEventType = assignMode == TaskAssignMode.ALL
                ? TimelineEventType.TASK_ASSIGNED_ALL
                : TimelineEventType.TASK_ASSIGNED_USER;
        saveEvent(
                TimelineScopeType.PROJECT,
                task.getPosting().getId(),
                TimelineEventType.TASK_CREATED,
                actor,
                task,
                null,
                projectPayload(stats)
        );
        saveEvent(
                TimelineScopeType.PROJECT,
                task.getPosting().getId(),
                assignEventType,
                actor,
                task,
                null,
                projectPayloadWithAssignedUsers(stats, assignments)
        );
    }

    @Transactional
    public void recordAssignmentSubmitted(UserAccount actor, TaskAssignment assignment) {
        TaskTimelineStatsResponse stats = buildProjectStats(assignment.getTask().getPosting().getId());
        String payload = assignmentPayload(assignment.getStatus(), stats);
        saveEvent(
                TimelineScopeType.PROJECT,
                assignment.getTask().getPosting().getId(),
                TimelineEventType.TASK_SUBMITTED,
                actor,
                assignment.getTask(),
                assignment,
                payload
        );
        saveEvent(
                TimelineScopeType.USER,
                assignment.getUser().getId(),
                TimelineEventType.TASK_SUBMITTED,
                actor,
                assignment.getTask(),
                assignment,
                payload
        );
    }

    @Transactional
    public void recordAssignmentReviewed(UserAccount actor, TaskAssignment assignment, TimelineEventType eventType) {
        TaskTimelineStatsResponse stats = buildProjectStats(assignment.getTask().getPosting().getId());
        String payload = assignmentPayload(assignment.getStatus(), stats);
        saveEvent(
                TimelineScopeType.PROJECT,
                assignment.getTask().getPosting().getId(),
                eventType,
                actor,
                assignment.getTask(),
                assignment,
                payload
        );
        saveEvent(
                TimelineScopeType.USER,
                assignment.getUser().getId(),
                eventType,
                actor,
                assignment.getTask(),
                assignment,
                payload
        );
    }

    @Transactional
    public void recordTaskScopeUpdated(UserAccount actor, ProjectTask task, TaskAssignMode assignMode, List<TaskAssignment> assignments) {
        TaskTimelineStatsResponse stats = buildProjectStats(task.getPosting().getId());
        TimelineEventType assignEventType = assignMode == TaskAssignMode.ALL
                ? TimelineEventType.TASK_ASSIGNED_ALL
                : TimelineEventType.TASK_ASSIGNED_USER;
        saveEvent(
                TimelineScopeType.PROJECT,
                task.getPosting().getId(),
                assignEventType,
                actor,
                task,
                null,
                projectPayloadWithAssignedUsers(stats, assignments)
        );
    }

    @Transactional(readOnly = true)
    public Page<TaskTimelineEventResponse> getProjectTimeline(Long projectId, Integer page, Integer size) {
        return findByScope(TimelineScopeType.PROJECT, projectId, page, size)
                .map(event -> toResponse(event, true));
    }

    @Transactional(readOnly = true)
    public Page<TaskTimelineEventResponse> getUserTimeline(Long userId, Integer page, Integer size) {
        return findByScope(TimelineScopeType.USER, userId, page, size)
                .map(event -> toResponse(event, false));
    }

    private Page<TimelineEvent> findByScope(TimelineScopeType scopeType, Long scopeId, Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return timelineEventRepository.findByScopeTypeAndScopeId(scopeType, scopeId, pageable);
    }

    private TaskTimelineEventResponse toResponse(TimelineEvent event, boolean includeStats) {
        TaskTimelineActorResponse actor = toActor(event.getActorUser());
        TaskTimelineTaskResponse task = new TaskTimelineTaskResponse(event.getTask().getId(), event.getTask().getTitle());
        TaskTimelineAssignmentResponse assignment = null;
        if (event.getAssignment() != null) {
            TaskAssignmentStatus statusFromPayload = readAssignmentStatus(event.getPayloadJson());
            assignment = new TaskTimelineAssignmentResponse(
                    event.getAssignment().getId(),
                    statusFromPayload != null ? statusFromPayload : event.getAssignment().getStatus()
            );
        }
        TaskTimelineStatsResponse stats = includeStats ? readStats(event.getPayloadJson()) : null;
        return new TaskTimelineEventResponse(
                event.getEventType(),
                event.getCreatedAt(),
                actor,
                task,
                assignment,
                stats
        );
    }

    private TaskTimelineActorResponse toActor(UserAccount user) {
        String firstName = user.getProfile() != null ? user.getProfile().getFirstName() : null;
        String lastName = user.getProfile() != null ? user.getProfile().getLastName() : null;
        return new TaskTimelineActorResponse(user.getId(), firstName, lastName, user.getEmail());
    }

    private TaskTimelineStatsResponse buildProjectStats(Long projectId) {
        long assigned = taskAssignmentRepository.countByTaskPostingId(projectId);
        long approved = taskAssignmentRepository.countByTaskPostingIdAndStatusIn(
                projectId,
                List.of(TaskAssignmentStatus.APPROVED, TaskAssignmentStatus.DONE)
        );
        long rejected = taskAssignmentRepository.countByTaskPostingIdAndStatusIn(
                projectId,
                List.of(TaskAssignmentStatus.REJECTED, TaskAssignmentStatus.FAILED)
        );
        long pending = Math.max(0, assigned - approved - rejected);
        return new TaskTimelineStatsResponse(assigned, approved, rejected, pending);
    }

    private void saveEvent(
            TimelineScopeType scopeType,
            Long scopeId,
            TimelineEventType eventType,
            UserAccount actor,
            ProjectTask task,
            TaskAssignment assignment,
            String payloadJson
    ) {
        TimelineEvent event = new TimelineEvent();
        event.setScopeType(scopeType);
        event.setScopeId(scopeId);
        event.setEventType(eventType);
        event.setActorUser(actor);
        event.setTask(task);
        event.setAssignment(assignment);
        event.setPayloadJson(payloadJson);
        timelineEventRepository.save(event);
    }

    private String projectPayload(TaskTimelineStatsResponse stats) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stats", stats);
        return serialize(payload);
    }

    private String projectPayloadWithAssignedUsers(TaskTimelineStatsResponse stats, Collection<TaskAssignment> assignments) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stats", stats);
        payload.put("assignedUserIds", assignments.stream().map(a -> a.getUser().getId()).toList());
        return serialize(payload);
    }

    private String assignmentPayload(TaskAssignmentStatus assignmentStatus, TaskTimelineStatsResponse stats) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assignmentStatus", assignmentStatus);
        payload.put("stats", stats);
        return serialize(payload);
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private TaskTimelineStatsResponse readStats(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            JsonNode stats = root.get("stats");
            if (stats == null || !stats.isObject()) {
                return null;
            }
            return new TaskTimelineStatsResponse(
                    stats.path("assigned").asLong(0),
                    stats.path("approved").asLong(0),
                    stats.path("rejected").asLong(0),
                    stats.path("pending").asLong(0)
            );
        } catch (Exception ex) {
            return null;
        }
    }

    private TaskAssignmentStatus readAssignmentStatus(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            String raw = root.path("assignmentStatus").asText(null);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return TaskAssignmentStatus.valueOf(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}
