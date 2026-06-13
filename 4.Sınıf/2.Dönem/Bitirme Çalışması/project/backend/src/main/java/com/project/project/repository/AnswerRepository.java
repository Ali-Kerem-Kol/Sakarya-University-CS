package com.project.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.project.entity.Answer;

/**
 * Data access for question answers.
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByQuestionId(Long questionId);
}

