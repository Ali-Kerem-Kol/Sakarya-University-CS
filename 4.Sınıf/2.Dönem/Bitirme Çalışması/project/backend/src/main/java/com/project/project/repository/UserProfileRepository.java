package com.project.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.UserProfile;

/**
 * Data access layer for user profiles.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    java.util.Optional<UserProfile> findByUserAccountId(Long userId);
}
