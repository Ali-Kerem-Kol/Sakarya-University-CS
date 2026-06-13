package com.project.project.service.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.task.TaskAssignMode;
import com.project.project.dto.task.TaskGraphBranchResponse;
import com.project.project.dto.task.TaskGraphEdgeResponse;
import com.project.project.dto.task.TaskGraphNodeResponse;
import com.project.project.dto.task.TaskGraphNodeStatus;
import com.project.project.dto.task.TaskGraphResponse;
import com.project.project.entity.ProjectTask;
import com.project.project.entity.TaskAssignment;
import com.project.project.entity.TaskAssignmentStatus;
import com.project.project.entity.TimelineEvent;
import com.project.project.entity.TimelineEventType;
import com.project.project.entity.TimelineScopeType;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ProjectTaskRepository;
import com.project.project.repository.TaskAssignmentRepository;
import com.project.project.repository.TimelineEventRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.util.UserColorResolver;

/**
 * Builds admin/student graph payload for task branches.
 */
@Service
public class TaskGraphService {

    private static final String MAIN_BRANCH = "MAIN";

    private final ProjectTaskRepository projectTaskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final UserAccountRepository userAccountRepository;

    public TaskGraphService(
            ProjectTaskRepository projectTaskRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            TimelineEventRepository timelineEventRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.projectTaskRepository = projectTaskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.timelineEventRepository = timelineEventRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public TaskGraphResponse getAdminProjectGraph(Long projectId) {
        return buildProjectGraph(projectId, null);
    }

    @Transactional(readOnly = true)
    public TaskGraphResponse getStudentProjectGraph(Long userId, Long projectId) {
        Long resolvedProjectId = resolveStudentProjectId(userId, projectId);
        return buildProjectGraph(resolvedProjectId, userId);
    }

    private Long resolveStudentProjectId(Long userId, Long projectId) {
        if (projectId != null) {
            return projectId;
        }
        List<TaskAssignment> myAssignments = taskAssignmentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (myAssignments.isEmpty()) {
            throw new NotFoundException("No task assignments found for current user");
        }
        Set<Long> projectIds = myAssignments.stream()
                .map(assignment -> assignment.getTask().getPosting().getId())
                .collect(Collectors.toSet());
        if (projectIds.size() > 1) {
            throw new BadRequestException("projectId is required when user has assignments in multiple projects");
        }
        return projectIds.iterator().next();
    }

    private TaskGraphResponse buildProjectGraph(Long projectId, Long onlyUserId) {
        List<ProjectTask> tasks = projectTaskRepository.findByPostingIdOrderByCreatedAtDesc(projectId);
        if (tasks.isEmpty()) {
            throw new NotFoundException("Project not found or has no tasks");
        }

        Map<Long, TaskAssignMode> assignModes = resolveAssignModes(projectId, tasks);
        List<TaskAssignment> assignments = onlyUserId == null
                ? taskAssignmentRepository.findByTaskPostingIdOrderByCreatedAtDesc(projectId)
                : taskAssignmentRepository.findByTaskPostingIdAndUserIdOrderByCreatedAtDesc(projectId, onlyUserId);
        Map<Long, List<TaskAssignment>> assignmentsByTask = assignments.stream()
                .collect(Collectors.groupingBy(assignment -> assignment.getTask().getId()));

        Map<Long, UserAccount> usersById = resolveUsersById(assignments, onlyUserId);
        Map<Long, String> userColorsById = resolveUserColors(usersById);

        List<NodeRef> mainNodes = new ArrayList<>();
        Map<Long, List<NodeRef>> userBranchNodes = new LinkedHashMap<>();

        for (ProjectTask task : tasks) {
            TaskAssignMode mode = assignModes.getOrDefault(task.getId(), inferAssignMode(task));
            List<TaskAssignment> taskAssignments = assignmentsByTask.getOrDefault(task.getId(), List.of());

            if (mode == TaskAssignMode.ALL) {
                if (onlyUserId != null && taskAssignments.isEmpty()) {
                    continue;
                }
                mainNodes.add(new NodeRef(new TaskGraphNodeResponse(
                        task.getId(),
                        MAIN_BRANCH,
                        task.getCreatedAt(),
                        task.getTitle(),
                        aggregateMainStatus(taskAssignments),
                        task.getCreatedBy().getId(),
                        onlyUserId,
                        onlyUserId != null ? userColorsById.get(onlyUserId) : null
                )));
                continue;
            }

            for (TaskAssignment assignment : taskAssignments) {
                Long assignedUserId = assignment.getUser().getId();
                String branchKey = branchKeyForUser(assignedUserId);
                TaskGraphNodeResponse node = new TaskGraphNodeResponse(
                        task.getId(),
                        branchKey,
                        assignment.getAssignedAt() != null ? assignment.getAssignedAt() : task.getCreatedAt(),
                        task.getTitle(),
                        mapAssignmentStatus(assignment.getStatus()),
                        task.getCreatedBy().getId(),
                        assignedUserId,
                        userColorsById.get(assignedUserId)
                );
                userBranchNodes.computeIfAbsent(assignedUserId, key -> new ArrayList<>()).add(new NodeRef(node));
            }
        }

        Comparator<NodeRef> byCreatedAtAsc = Comparator
                .comparing((NodeRef ref) -> ref.node().createdAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ref -> ref.node().taskId());
        mainNodes.sort(byCreatedAtAsc);
        userBranchNodes.values().forEach(nodes -> nodes.sort(byCreatedAtAsc));

        List<TaskGraphEdgeResponse> edges = new ArrayList<>();
        edges.addAll(chainEdges(mainNodes));
        for (List<NodeRef> branchNodes : userBranchNodes.values()) {
            edges.addAll(chainEdges(branchNodes));
        }

        List<TaskGraphNodeResponse> nodes = new ArrayList<>();
        nodes.addAll(mainNodes.stream().map(NodeRef::node).toList());
        for (List<NodeRef> branchNodes : userBranchNodes.values()) {
            nodes.addAll(branchNodes.stream().map(NodeRef::node).toList());
        }
        nodes.sort(Comparator.comparing(TaskGraphNodeResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(TaskGraphNodeResponse::taskId));

        List<TaskGraphBranchResponse> branches = new ArrayList<>();
        branches.add(new TaskGraphBranchResponse(MAIN_BRANCH, "Main (Genel Gorevler)", null, null));
        for (Long userId : userBranchNodes.keySet()) {
            String name = resolveDisplayName(usersById.get(userId), userId);
            branches.add(new TaskGraphBranchResponse(
                    branchKeyForUser(userId),
                    "Ogrenci: " + name,
                    userId,
                    userColorsById.get(userId)
            ));
        }

        return new TaskGraphResponse(projectId, branches, nodes, edges);
    }

    private Map<Long, UserAccount> resolveUsersById(List<TaskAssignment> assignments, Long onlyUserId) {
        Set<Long> userIds = assignments.stream()
                .map(assignment -> assignment.getUser().getId())
                .collect(Collectors.toCollection(HashSet::new));
        if (onlyUserId != null) {
            userIds.add(onlyUserId);
        }
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userAccountRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserAccount::getId, user -> user));
    }

    private Map<Long, String> resolveUserColors(Map<Long, UserAccount> usersById) {
        Map<Long, String> result = new HashMap<>();
        usersById.forEach((userId, account) -> result.put(
                userId,
                UserColorResolver.resolveDisplayColor(userId, account.getPreferredColor())
        ));
        return result;
    }

    private List<TaskGraphEdgeResponse> chainEdges(List<NodeRef> sortedNodes) {
        List<TaskGraphEdgeResponse> edges = new ArrayList<>();
        for (int i = 1; i < sortedNodes.size(); i++) {
            edges.add(new TaskGraphEdgeResponse(
                    sortedNodes.get(i - 1).node().taskId(),
                    sortedNodes.get(i).node().taskId()
            ));
        }
        return edges;
    }

    private Map<Long, TaskAssignMode> resolveAssignModes(Long projectId, List<ProjectTask> tasks) {
        Map<Long, TaskAssignMode> modes = new LinkedHashMap<>();
        Set<Long> taskIds = tasks.stream().map(ProjectTask::getId).collect(Collectors.toSet());
        List<TimelineEvent> events = timelineEventRepository
                .findByScopeTypeAndScopeIdOrderByCreatedAtAscIdAsc(TimelineScopeType.PROJECT, projectId);
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

    private TaskAssignMode inferAssignMode(ProjectTask task) {
        if (task.getAssignments() == null || task.getAssignments().isEmpty()) {
            return TaskAssignMode.ALL;
        }
        return task.getAssignments().size() > 1 ? TaskAssignMode.ALL : TaskAssignMode.USER;
    }

    private TaskGraphNodeStatus aggregateMainStatus(List<TaskAssignment> assignments) {
        if (assignments.isEmpty()) {
            return TaskGraphNodeStatus.PENDING;
        }
        boolean allSuccess = assignments.stream().allMatch(this::isSuccessLike);
        if (allSuccess) {
            return TaskGraphNodeStatus.SUCCESS;
        }
        if (assignments.stream().anyMatch(a -> a.getStatus() == TaskAssignmentStatus.REVISION_REQUESTED)) {
            return TaskGraphNodeStatus.REVISION_REQUESTED;
        }
        if (assignments.stream().anyMatch(this::isFailedLike)) {
            return TaskGraphNodeStatus.FAILED;
        }
        return TaskGraphNodeStatus.PENDING;
    }

    private TaskGraphNodeStatus mapAssignmentStatus(TaskAssignmentStatus status) {
        return switch (status) {
            case APPROVED, DONE -> TaskGraphNodeStatus.SUCCESS;
            case REJECTED, FAILED -> TaskGraphNodeStatus.FAILED;
            case REVISION_REQUESTED -> TaskGraphNodeStatus.REVISION_REQUESTED;
            case SUBMITTED -> TaskGraphNodeStatus.SUBMITTED;
            default -> TaskGraphNodeStatus.PENDING;
        };
    }

    private boolean isSuccessLike(TaskAssignment assignment) {
        return assignment.getStatus() == TaskAssignmentStatus.APPROVED
                || assignment.getStatus() == TaskAssignmentStatus.DONE;
    }

    private boolean isFailedLike(TaskAssignment assignment) {
        return assignment.getStatus() == TaskAssignmentStatus.REJECTED
                || assignment.getStatus() == TaskAssignmentStatus.FAILED;
    }

    private String branchKeyForUser(Long userId) {
        return "USER-" + userId;
    }

    private String resolveDisplayName(UserAccount account, Long userId) {
        if (account == null) {
            return "User " + userId;
        }
        if (account.getProfile() != null && account.getProfile().getFirstName() != null
                && !account.getProfile().getFirstName().isBlank()) {
            return account.getProfile().getFirstName();
        }
        return account.getEmail();
    }

    private record NodeRef(TaskGraphNodeResponse node) {
    }
}
