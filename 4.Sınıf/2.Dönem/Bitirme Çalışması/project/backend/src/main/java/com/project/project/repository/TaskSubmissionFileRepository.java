package com.project.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.TaskSubmissionFile;

/**
 * Data access for task assignment submission files.
 */
public interface TaskSubmissionFileRepository extends JpaRepository<TaskSubmissionFile, Long> {

    @EntityGraph(attributePaths = {"document"})
    List<TaskSubmissionFile> findByAssignmentIdOrderByCreatedAtAsc(Long assignmentId);

    @EntityGraph(attributePaths = {"document", "assignment", "assignment.user"})
    Optional<TaskSubmissionFile> findByIdAndAssignmentId(Long id, Long assignmentId);
}
