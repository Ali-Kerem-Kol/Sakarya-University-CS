package com.project.project.service.qa;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.qa.AdminQuestionResponse;
import com.project.project.dto.qa.AdminQuestionStatusFilter;
import com.project.project.dto.qa.AnswerRequest;
import com.project.project.dto.qa.PublicQuestionResponse;
import com.project.project.dto.qa.PublishQuestionRequest;
import com.project.project.dto.qa.QuestionCreateRequest;
import com.project.project.dto.qa.QuestionResponse;
import com.project.project.dto.qa.StudentQuestionCreateRequest;
import com.project.project.dto.qa.StudentQuestionResponse;
import com.project.project.entity.Answer;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.PublishScope;
import com.project.project.entity.Question;
import com.project.project.entity.UserAccount;
import com.project.project.repository.AnswerRepository;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.QuestionRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.policy.PostingVisibilityPolicy;

/**
 * Q&A flows with anonymous asker visibility.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ApplicationPostingRepository postingRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final PostingVisibilityPolicy postingVisibilityPolicy;

    public QuestionService(
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            ApplicationPostingRepository postingRepository,
            ApplicationSubmissionRepository submissionRepository,
            UserAccountRepository userAccountRepository,
            PostingVisibilityPolicy postingVisibilityPolicy
    ) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.postingRepository = postingRepository;
        this.submissionRepository = submissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.postingVisibilityPolicy = postingVisibilityPolicy;
    }

    @Transactional
    public QuestionResponse ask(Long userId, Long postingId, QuestionCreateRequest request) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Question question = new Question();
        question.setPosting(posting);
        question.setAskedByUser(user);
        question.setQuestionText(request.questionText());
        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public StudentQuestionResponse ask(Long userId, StudentQuestionCreateRequest request) {
        ApplicationPosting posting = postingRepository.findById(request.postingId())
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Question question = new Question();
        question.setPosting(posting);
        question.setAskedByUser(user);
        question.setQuestionText(request.questionText());
        return toStudentResponse(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listMine(Long userId) {
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questionRepository.findByAskedByUserIdOrderByCreatedAtDesc(userId)) {
            responses.add(toResponse(question));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<StudentQuestionResponse> listMineV2(Long userId) {
        List<StudentQuestionResponse> responses = new ArrayList<>();
        for (Question question : questionRepository.findByAskedByUserIdOrderByCreatedAtDesc(userId)) {
            responses.add(toStudentResponse(question));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listByPostingForAdmin(Long postingId) {
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questionRepository.findByPostingIdOrderByCreatedAtDesc(postingId)) {
            responses.add(toResponse(question));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<AdminQuestionResponse> listForAdmin(Long postingId, AdminQuestionStatusFilter status) {
        List<Question> source = postingId != null
                ? questionRepository.findByPostingIdOrderByCreatedAtDesc(postingId)
                : questionRepository.findAllByOrderByCreatedAtDesc();
        List<AdminQuestionResponse> responses = new ArrayList<>();
        for (Question question : source) {
            if (!matchesStatus(question, status)) {
                continue;
            }
            responses.add(toAdminResponse(question));
        }
        return responses;
    }

    @Transactional
    public QuestionResponse answer(Long adminId, Long questionId, AnswerRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        Answer answer = answerRepository.findByQuestionId(questionId).orElseGet(Answer::new);
        answer.setQuestion(question);
        answer.setAnsweredByAdmin(admin);
        answer.setAnswerText(request.answerText());
        answerRepository.save(answer);
        return toResponse(questionRepository.findById(questionId).orElse(question));
    }

    @Transactional
    public QuestionResponse publish(Long questionId, PublishQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        PublishScope scope = resolveScope(request);
        if (scope == PublishScope.PUBLIC && question.getAnswer() == null) {
            throw new BadRequestException("QUESTION_MUST_BE_ANSWERED_BEFORE_PUBLISH");
        }
        question.setPublishScope(scope);
        question.setPublishedAt(scope == PublishScope.PUBLIC ? java.time.Instant.now() : null);
        return toResponse(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listPublic() {
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questionRepository.findByPublishScopeOrderByCreatedAtDesc(PublishScope.PUBLIC)) {
            responses.add(toResponse(question));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<PublicQuestionResponse> listPublishedByPosting(Long postingId) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        postingVisibilityPolicy.assertPublicReadable(posting);
        List<PublicQuestionResponse> responses = new ArrayList<>();
        for (Question question : questionRepository.findByPostingIdAndPublishScopeOrderByCreatedAtDesc(
                postingId, PublishScope.PUBLIC)) {
            if (question.getAnswer() == null) {
                continue;
            }
            responses.add(toPublicResponse(question));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listProjectOnly(Long postingId, Long requesterUserId) {
        boolean hasSubmission = submissionRepository.existsByPostingIdAndUserId(postingId, requesterUserId);
        if (!hasSubmission) {
            throw new AccessDeniedException("PROJECT_FEED_ACCESS_DENIED");
        }
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question question : questionRepository.findByPostingIdAndPublishScopeOrderByCreatedAtDesc(
                postingId, PublishScope.PROJECT_ONLY)) {
            responses.add(toResponse(question));
        }
        return responses;
    }

    @Transactional
    public void deleteByAdmin(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new NotFoundException("Question not found");
        }
        questionRepository.deleteById(questionId);
    }

    private QuestionResponse toResponse(Question question) {
        Answer answer = question.getAnswer();
        return new QuestionResponse(
                question.getId(),
                question.getPosting().getId(),
                question.getQuestionText(),
                answer != null ? answer.getAnswerText() : null,
                question.getPublishScope(),
                question.getCreatedAt()
        );
    }

    private StudentQuestionResponse toStudentResponse(Question question) {
        Answer answer = question.getAnswer();
        return new StudentQuestionResponse(
                question.getId(),
                question.getPosting().getId(),
                question.getQuestionText(),
                question.getCreatedAt(),
                answer != null ? answer.getUpdatedAt() : null,
                answer != null ? answer.getAnswerText() : null,
                question.getPublishScope() == PublishScope.PUBLIC
        );
    }

    private AdminQuestionResponse toAdminResponse(Question question) {
        Answer answer = question.getAnswer();
        String firstName = question.getAskedByUser().getProfile() != null
                ? question.getAskedByUser().getProfile().getFirstName() : null;
        String lastName = question.getAskedByUser().getProfile() != null
                ? question.getAskedByUser().getProfile().getLastName() : null;
        String askedByName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        if (askedByName.isBlank()) {
            askedByName = "N/A";
        }
        return new AdminQuestionResponse(
                question.getId(),
                question.getPosting().getId(),
                question.getPosting().getTitle(),
                question.getQuestionText(),
                question.getCreatedAt(),
                question.getAskedByUser().getId(),
                question.getAskedByUser().getEmail(),
                askedByName,
                answer != null ? answer.getAnswerText() : null,
                question.getPublishScope() == PublishScope.PUBLIC,
                question.getPublishedAt()
        );
    }

    private PublicQuestionResponse toPublicResponse(Question question) {
        return new PublicQuestionResponse(
                question.getId(),
                question.getPosting().getId(),
                question.getQuestionText(),
                question.getAnswer() != null ? question.getAnswer().getAnswerText() : null,
                question.getPublishedAt()
        );
    }

    private boolean matchesStatus(Question question, AdminQuestionStatusFilter status) {
        if (status == null) {
            return true;
        }
        return switch (status) {
            case ANSWERED -> question.getAnswer() != null;
            case UNANSWERED -> question.getAnswer() == null;
            case PUBLISHED -> question.getPublishScope() == PublishScope.PUBLIC;
            case UNPUBLISHED -> question.getPublishScope() != PublishScope.PUBLIC;
        };
    }

    private PublishScope resolveScope(PublishQuestionRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid publish request");
        }
        if (request.scope() != null) {
            return request.scope();
        }
        if (request.published() != null) {
            return request.published() ? PublishScope.PUBLIC : PublishScope.PRIVATE;
        }
        throw new BadRequestException("Either scope or published must be provided");
    }
}
