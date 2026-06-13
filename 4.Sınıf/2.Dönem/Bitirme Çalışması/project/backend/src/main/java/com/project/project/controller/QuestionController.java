package com.project.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import com.project.project.dto.qa.AdminQuestionResponse;
import com.project.project.dto.qa.AdminQuestionStatusFilter;
import com.project.project.dto.qa.AnswerRequest;
import com.project.project.dto.qa.PublicQuestionResponse;
import com.project.project.dto.qa.PublishQuestionRequest;
import com.project.project.dto.qa.QuestionCreateRequest;
import com.project.project.dto.qa.QuestionResponse;
import com.project.project.dto.qa.StudentQuestionCreateRequest;
import com.project.project.dto.qa.StudentQuestionResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.qa.QuestionService;

/**
 * Q&A endpoints for user/admin/public scopes.
 */
@RestController
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/api/v1/me/postings/{postingId}/questions")
    public ResponseEntity<QuestionResponse> askQuestion(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long postingId,
            @Valid @RequestBody QuestionCreateRequest request
    ) {
        return ResponseEntity.status(201)
                .body(questionService.ask(principal.getUserAccount().getId(), postingId, request));
    }

    @PostMapping("/api/v1/questions")
    public ResponseEntity<StudentQuestionResponse> askQuestionV2(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody StudentQuestionCreateRequest request
    ) {
        return ResponseEntity.status(201)
                .body(questionService.ask(principal.getUserAccount().getId(), request));
    }

    @GetMapping("/api/v1/me/questions")
    public ResponseEntity<List<QuestionResponse>> myQuestions(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(questionService.listMine(principal.getUserAccount().getId()));
    }

    @GetMapping("/api/v1/questions/my")
    public ResponseEntity<List<StudentQuestionResponse>> myQuestionsV2(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(questionService.listMineV2(principal.getUserAccount().getId()));
    }

    @GetMapping("/api/v1/admin/postings/{postingId}/questions")
    public ResponseEntity<List<QuestionResponse>> adminPostingQuestions(@PathVariable Long postingId) {
        return ResponseEntity.ok(questionService.listByPostingForAdmin(postingId));
    }

    @GetMapping("/api/v1/admin/questions")
    public ResponseEntity<List<AdminQuestionResponse>> adminQuestions(
            @RequestParam(required = false) Long postingId,
            @RequestParam(required = false) AdminQuestionStatusFilter status
    ) {
        return ResponseEntity.ok(questionService.listForAdmin(postingId, status));
    }

    @PostMapping("/api/v1/admin/questions/{id}/answer")
    public ResponseEntity<QuestionResponse> answer(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request
    ) {
        return ResponseEntity.ok(questionService.answer(principal.getUserAccount().getId(), id, request));
    }

    @PostMapping("/api/v1/admin/questions/{id}/publish")
    public ResponseEntity<QuestionResponse> publish(
            @PathVariable Long id,
            @Valid @RequestBody PublishQuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.publish(id, request));
    }

    @DeleteMapping("/api/v1/admin/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/public/questions")
    public ResponseEntity<List<QuestionResponse>> publicQuestions() {
        return ResponseEntity.ok(questionService.listPublic());
    }

    @GetMapping("/api/v1/postings/{postingId}/questions")
    public ResponseEntity<List<QuestionResponse>> postingQuestions(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long postingId
    ) {
        return ResponseEntity.ok(questionService.listProjectOnly(postingId, principal.getUserAccount().getId()));
    }

    @GetMapping("/api/v1/postings/{postingId}/qa")
    public ResponseEntity<List<PublicQuestionResponse>> publicPostingQa(@PathVariable Long postingId) {
        return ResponseEntity.ok(questionService.listPublishedByPosting(postingId));
    }
}
