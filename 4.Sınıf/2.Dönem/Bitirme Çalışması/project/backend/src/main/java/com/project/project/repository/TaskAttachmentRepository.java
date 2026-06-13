package com.project.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.TaskAttachment;

/**
 * Data access for task attachments.
 */
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    @EntityGraph(attributePaths = {"document"})
    List<TaskAttachment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    @EntityGraph(attributePaths = {"document", "task"})
    Optional<TaskAttachment> findByIdAndTaskId(Long id, Long taskId);
}
