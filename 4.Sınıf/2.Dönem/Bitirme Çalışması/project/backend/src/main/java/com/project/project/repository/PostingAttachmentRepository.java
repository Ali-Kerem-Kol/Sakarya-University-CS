package com.project.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.PostingAttachment;

/**
 * Data access for posting attachments.
 */
public interface PostingAttachmentRepository extends JpaRepository<PostingAttachment, Long> {

    List<PostingAttachment> findByPostingId(Long postingId);

    Optional<PostingAttachment> findByIdAndPostingId(Long id, Long postingId);
}
