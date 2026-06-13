package com.project.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.MailJob;

/**
 * Data access for async mail jobs.
 */
public interface MailJobRepository extends JpaRepository<MailJob, Long> {
}
