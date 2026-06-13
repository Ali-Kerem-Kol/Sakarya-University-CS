package com.project.project.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.ApplicationSubmissionStatus;
import com.project.project.entity.ApplicationSubmission;
import com.project.project.repository.projection.AdminSubmissionRowProjection;

/**
 * Data access for application submissions.
 */
public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {

    boolean existsByPostingIdAndUserId(Long postingId, Long userId);

    java.util.Optional<ApplicationSubmission> findByPostingIdAndUserId(Long postingId, Long userId);

    boolean existsByPostingIdAndUserIdAndStatus(Long postingId, Long userId, ApplicationSubmissionStatus status);

    boolean existsByPostingIdAndUserIdAndStatusAndUserEnabledTrue(
            Long postingId,
            Long userId,
            ApplicationSubmissionStatus status
    );

    @EntityGraph(attributePaths = {"user", "user.profile"})
    List<ApplicationSubmission> findByPostingIdAndStatus(Long postingId, ApplicationSubmissionStatus status);

    @EntityGraph(attributePaths = {"user", "user.profile"})
    List<ApplicationSubmission> findByPostingIdAndStatusAndUserEnabledTrue(
            Long postingId,
            ApplicationSubmissionStatus status
    );

    List<ApplicationSubmission> findByPostingIdOrderByCreatedAtDesc(Long postingId);

    List<ApplicationSubmission> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            select distinct s.user.email
            from ApplicationSubmission s
            join s.posting p
            where p.category = :category
              and s.user.enabled = true
              and s.user.emailVerified = true
              and lower(s.user.email) like '%@ogr.sakarya.edu.tr'
            """)
    Set<String> findRecipientEmailsByCategory(@Param("category") ApplicationCategory category);

    @Query("""
            select distinct s.user.email
            from ApplicationSubmission s
            where s.posting.id = :postingId
              and s.user.enabled = true
              and s.user.emailVerified = true
              and lower(s.user.email) like '%@ogr.sakarya.edu.tr'
            """)
    Set<String> findRecipientEmailsByPostingId(@Param("postingId") Long postingId);

    @Query(value = """
            select
                s.id as submissionId,
                p.id as postingId,
                p.title as postingTitle,
                p.category as postingCategory,
                p.status as postingStatus,
                s.status as submissionStatus,
                s.submittedAt as submittedAt,
                u.id as userId,
                u.email as userEmail,
                up.firstName as profileFirstName,
                up.lastName as profileLastName,
                s.profileSnapshotJson as profileSnapshotJson,
                s.cvDocumentIdSnapshot as cvDocumentIdSnapshot
            from ApplicationSubmission s
            join s.posting p
            join s.user u
            left join UserProfile up on up.userAccount = u
            where (:category is null or p.category = :category)
              and (:submissionStatus is null or s.status = :submissionStatus)
              and (:postingStatus is null or p.status = :postingStatus)
              and (:postingId is null or p.id = :postingId)
            """,
            countQuery = """
            select count(s)
            from ApplicationSubmission s
            join s.posting p
            where (:category is null or p.category = :category)
              and (:submissionStatus is null or s.status = :submissionStatus)
              and (:postingStatus is null or p.status = :postingStatus)
              and (:postingId is null or p.id = :postingId)
            """)
    Page<AdminSubmissionRowProjection> findAdminSubmissionRows(
            @Param("category") ApplicationCategory category,
            @Param("submissionStatus") ApplicationSubmissionStatus submissionStatus,
            @Param("postingStatus") ApplicationPostingStatus postingStatus,
            @Param("postingId") Long postingId,
            Pageable pageable
    );
}
