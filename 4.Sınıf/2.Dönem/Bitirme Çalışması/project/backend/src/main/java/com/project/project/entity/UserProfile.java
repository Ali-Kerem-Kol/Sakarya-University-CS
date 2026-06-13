package com.project.project.entity;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores user-owned profile details and related artifacts like cv and availability.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Column(length = 80)
    private String firstName;

    @Column(length = 80)
    private String lastName;

    private Integer classYear;

    @Column(length = 120)
    private String department;

    @Column(length = 40)
    private String englishLevel;

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    @Column(length = 30)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_document_id")
    private Document cvDocument;

    @OneToMany(mappedBy = "profile")
    private List<UserAvailabilitySlot> availabilitySlots = new ArrayList<>();

}
