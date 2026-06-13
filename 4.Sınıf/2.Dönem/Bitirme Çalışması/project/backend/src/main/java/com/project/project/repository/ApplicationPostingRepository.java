package com.project.project.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;

/**
 * Data access for posting aggregate root.
 */
public interface ApplicationPostingRepository extends JpaRepository<ApplicationPosting, Long> {

    java.util.Optional<ApplicationPosting> findByTitleIgnoreCase(String title);

    List<ApplicationPosting> findByCategoryAndStatus(ApplicationCategory category, ApplicationPostingStatus status);

    List<ApplicationPosting> findByStatus(ApplicationPostingStatus status);

    List<ApplicationPosting> findByCategory(ApplicationCategory category);

    List<ApplicationPosting> findByStatusIn(Collection<ApplicationPostingStatus> statuses);

    List<ApplicationPosting> findByCategoryAndStatusIn(
            ApplicationCategory category,
            Collection<ApplicationPostingStatus> statuses
    );
}
