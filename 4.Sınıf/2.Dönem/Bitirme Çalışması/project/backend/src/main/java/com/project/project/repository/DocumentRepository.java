package com.project.project.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.Document;

/**
 * Data access layer for unified document metadata.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUploadedByIdOrderByUploadedAtDesc(Long userId);

    java.util.Optional<Document> findByIdAndUploadedById(Long id, Long userId);

    @Query("""
            select distinct d.uploadedBy.id from Document d
            where d.uploadedBy.id in :userIds and d.storageKey like 'users/%/cv/%'
            """)
    Set<Long> findCvUserIdsWithDocuments(List<Long> userIds);
}
