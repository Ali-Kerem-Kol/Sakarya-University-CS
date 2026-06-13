package com.project.project.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.project.project.config.exception.InvalidStatusTransitionException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregate root for posting lifecycle, attachments, and submissions.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "application_postings")
public class ApplicationPosting extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false, length = 200)
    private String projectName;

    @Column(nullable = false, length = 5000)
    private String projectDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationPostingStatus status = ApplicationPostingStatus.DRAFT;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = false)
    private UserAccount createdByAdmin;

    private Instant publishedAt;

    private Instant closedAt;

    @OneToMany(mappedBy = "posting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostingAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "posting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationSubmission> submissions = new ArrayList<>();

    public void changeStatus(ApplicationPostingStatus targetStatus) {
        if (status == targetStatus) {
            return;
        }
        if (status == ApplicationPostingStatus.DRAFT && targetStatus == ApplicationPostingStatus.PUBLISHED) {
            status = targetStatus;
            publishedAt = Instant.now();
            return;
        }
        if (status == ApplicationPostingStatus.PUBLISHED && targetStatus == ApplicationPostingStatus.CLOSED) {
            status = targetStatus;
            closedAt = Instant.now();
            return;
        }
        if (status == ApplicationPostingStatus.CLOSED && targetStatus == ApplicationPostingStatus.PUBLISHED) {
            status = targetStatus;
            closedAt = null;
            if (publishedAt == null) {
                publishedAt = Instant.now();
            }
            return;
        }
        throw new InvalidStatusTransitionException("Invalid posting status transition");
    }
}
