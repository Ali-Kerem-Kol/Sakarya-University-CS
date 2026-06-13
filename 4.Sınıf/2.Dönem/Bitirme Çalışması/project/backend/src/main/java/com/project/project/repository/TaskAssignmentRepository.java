package com.project.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.TaskAssignment;
import com.project.project.entity.TaskAssignmentStatus;

/**
 * Data access for task assignments.
 */
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    Optional<TaskAssignment> findByTaskIdAndUserId(Long taskId, Long userId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    List<TaskAssignment> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    List<TaskAssignment> findByUserIdAndTaskPostingIdOrderByUpdatedAtDesc(Long userId, Long postingId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    List<TaskAssignment> findByTaskPostingIdOrderByCreatedAtDesc(Long postingId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    List<TaskAssignment> findByTaskPostingIdAndUserIdOrderByCreatedAtDesc(Long postingId, Long userId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    List<TaskAssignment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    Optional<TaskAssignment> findByIdAndUserId(Long assignmentId, Long userId);

    @EntityGraph(attributePaths = {"task", "task.posting", "task.createdBy", "user", "user.profile"})
    Optional<TaskAssignment> findById(Long id);

    long countByTaskPostingId(Long postingId);

    long countByTaskPostingIdAndStatusIn(Long postingId, List<TaskAssignmentStatus> statuses);
}
