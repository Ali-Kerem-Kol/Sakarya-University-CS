package com.project.project.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Per-user assignment state for a task.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "task_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_task_assignment_task_user", columnNames = {"task_id", "user_id"})
)
public class TaskAssignment extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ProjectTask task;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskAssignmentStatus status = TaskAssignmentStatus.ASSIGNED;

    @Column(length = 1000)
    private String note;

    @Column(name = "text_answer", length = 5000)
    private String textAnswer;

    @Column(nullable = false)
    private Instant assignedAt = Instant.now();

    private Instant submittedAt;

    private Instant reviewedAt;

    @Column(length = 1000)
    private String reviewNote;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @jakarta.persistence.OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY,
            cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TaskSubmissionFile> submissionFiles = new java.util.ArrayList<>();
}
