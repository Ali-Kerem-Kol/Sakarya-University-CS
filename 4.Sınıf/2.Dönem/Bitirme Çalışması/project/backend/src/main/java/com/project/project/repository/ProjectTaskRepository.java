package com.project.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.ProjectTask;

/**
 * Data access for project tasks.
 */
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    @EntityGraph(attributePaths = {"posting", "createdBy", "assignments", "assignments.user", "assignments.user.profile"})
    List<ProjectTask> findByPostingIdOrderByCreatedAtDesc(Long postingId);

    @EntityGraph(attributePaths = {"posting", "createdBy"})
    Optional<ProjectTask> findById(Long id);

    @EntityGraph(attributePaths = {"posting", "createdBy", "assignments", "assignments.user", "assignments.user.profile"})
    Optional<ProjectTask> findByPostingIdAndTitleIgnoreCase(Long postingId, String title);
}
