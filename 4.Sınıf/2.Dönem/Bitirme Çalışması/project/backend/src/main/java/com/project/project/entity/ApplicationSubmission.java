package com.project.project.entity;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
 * User submission for a posting with immutable profile snapshot.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "application_submissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_submission_posting_user", columnNames = {"posting_id", "user_id"})
        }
)
public class ApplicationSubmission extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private ApplicationPosting posting;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private Instant submittedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationSubmissionStatus status = ApplicationSubmissionStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String profileSnapshotJson;

    @Column(nullable = false)
    private Integer snapshotVersion = 1;

    private Long cvDocumentIdSnapshot;
}
