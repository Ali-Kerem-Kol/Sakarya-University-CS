package com.project.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * File uploaded by student while submitting a task assignment.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "task_submission_files",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_task_submission_files_assignment_document",
                columnNames = {"task_assignment_id", "document_id"}
        )
)
public class TaskSubmissionFile extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_assignment_id", nullable = false)
    private TaskAssignment assignment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
}
