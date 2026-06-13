package com.project.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.PublishScope;
import com.project.project.entity.Question;

/**
 * Data access for posting questions.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = {"posting", "answer", "askedByUser", "askedByUser.profile"})
    List<Question> findByAskedByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"posting", "answer", "askedByUser", "askedByUser.profile"})
    List<Question> findByPostingIdOrderByCreatedAtDesc(Long postingId);

    @EntityGraph(attributePaths = {"posting", "answer", "askedByUser", "askedByUser.profile"})
    List<Question> findByPublishScopeOrderByCreatedAtDesc(PublishScope scope);

    @EntityGraph(attributePaths = {"posting", "answer", "askedByUser", "askedByUser.profile"})
    List<Question> findByPostingIdAndPublishScopeOrderByCreatedAtDesc(Long postingId, PublishScope scope);

    @EntityGraph(attributePaths = {"posting", "answer", "askedByUser", "askedByUser.profile"})
    List<Question> findAllByOrderByCreatedAtDesc();
}
