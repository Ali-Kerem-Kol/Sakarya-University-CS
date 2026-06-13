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
 * File attachment uploaded by admin for a task.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "task_attachments",
        uniqueConstraints = @UniqueConstraint(name = "uk_task_attachments_task_document", columnNames = {"task_id", "document_id"})
)
public class TaskAttachment extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ProjectTask task;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
}
