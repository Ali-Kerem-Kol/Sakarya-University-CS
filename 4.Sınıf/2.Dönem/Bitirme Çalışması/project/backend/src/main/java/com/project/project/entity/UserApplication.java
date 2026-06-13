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
 * Represents a user's application and its status, managed by admins.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_applications")
public class UserApplication extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @Column(nullable = false, length = 100)
    private String positionKey;

    @Column(length = 2000)
    private String motivationText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserApplicationStatus status = UserApplicationStatus.DRAFT;

    private java.time.Instant lastStatusChangedAt;
}
