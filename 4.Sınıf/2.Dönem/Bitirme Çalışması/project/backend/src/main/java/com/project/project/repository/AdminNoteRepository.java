package com.project.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.AdminNote;

/**
 * Data access layer for admin notes on user applications.
 */
public interface AdminNoteRepository extends JpaRepository<AdminNote, Long> {

    java.util.List<AdminNote> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
