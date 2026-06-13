package com.project.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks asynchronous admin mail jobs.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mail_jobs")
public class MailJob extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MailJobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MailJobStatus status = MailJobStatus.PENDING;

    @Column(length = 2000)
    private String payloadJson;

    @Column(length = 1000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private UserAccount createdByAdmin;
}
