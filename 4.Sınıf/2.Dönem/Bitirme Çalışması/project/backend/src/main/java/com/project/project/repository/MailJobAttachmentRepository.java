package com.project.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.MailJobAttachment;

/**
 * Repository for mail-job attachment links.
 */
public interface MailJobAttachmentRepository extends JpaRepository<MailJobAttachment, Long> {

    List<MailJobAttachment> findByMailJobId(Long mailJobId);
}
