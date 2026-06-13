package com.project.project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.TimelineEvent;
import com.project.project.entity.TimelineScopeType;

/**
 * Data access for timeline events.
 */
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {

    @EntityGraph(attributePaths = {"actorUser", "actorUser.profile", "task", "assignment"})
    Page<TimelineEvent> findByScopeTypeAndScopeId(TimelineScopeType scopeType, Long scopeId, Pageable pageable);

    @EntityGraph(attributePaths = {"actorUser", "actorUser.profile", "task", "assignment"})
    List<TimelineEvent> findByScopeTypeAndScopeIdOrderByCreatedAtAscIdAsc(TimelineScopeType scopeType, Long scopeId);

    void deleteByTaskId(Long taskId);
}
