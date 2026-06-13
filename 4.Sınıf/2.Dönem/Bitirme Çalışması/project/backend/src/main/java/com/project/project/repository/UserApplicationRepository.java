package com.project.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.UserApplication;
import com.project.project.entity.UserApplicationStatus;

/**
 * Data access layer for user applications.
 */
public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {

    boolean existsByUserAccountIdAndPositionKeyAndStatusIn(
            Long userId,
            String positionKey,
            Collection<UserApplicationStatus> statuses
    );

    List<UserApplication> findByUserAccountId(Long userId, Sort sort);

    Optional<UserApplication> findByIdAndUserAccountId(Long id, Long userId);

    List<UserApplication> findByStatus(UserApplicationStatus status, Sort sort);

    List<UserApplication> findByPositionKey(String positionKey, Sort sort);

    List<UserApplication> findByStatusAndPositionKey(UserApplicationStatus status, String positionKey, Sort sort);

    Page<UserApplication> findByStatus(UserApplicationStatus status, Pageable pageable);

    Page<UserApplication> findByPositionKey(String positionKey, Pageable pageable);

    Page<UserApplication> findByStatusAndPositionKey(UserApplicationStatus status, String positionKey, Pageable pageable);

    List<UserApplication> findByUserAccountIdInOrderByCreatedAtDesc(List<Long> userIds);

    List<UserApplication> findByUserAccountIdIn(List<Long> userIds, Sort sort);

    List<UserApplication> findByUserAccountIdInAndStatus(
            List<Long> userIds,
            UserApplicationStatus status,
            Sort sort
    );

    List<UserApplication> findByUserAccountIdInAndPositionKey(
            List<Long> userIds,
            String positionKey,
            Sort sort
    );

    List<UserApplication> findByUserAccountIdInAndStatusAndPositionKey(
            List<Long> userIds,
            UserApplicationStatus status,
            String positionKey,
            Sort sort
    );
}
