package com.project.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Student question linked to a posting.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "posting_questions")
public class Question extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private ApplicationPosting posting;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asked_by_user_id", nullable = false)
    private UserAccount askedByUser;

    @Column(nullable = false, length = 5000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublishScope publishScope = PublishScope.PRIVATE;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToOne(mappedBy = "question", fetch = FetchType.LAZY)
    private Answer answer;
}
