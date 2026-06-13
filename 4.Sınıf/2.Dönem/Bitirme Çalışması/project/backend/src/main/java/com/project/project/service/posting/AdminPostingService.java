package com.project.project.service.posting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.posting.AdminPostingUpsertRequest;
import com.project.project.dto.posting.PostingListResponse;
import com.project.project.dto.posting.PostingResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.policy.PostingVisibilityPolicy;

/**
 * Admin-facing posting management service.
 */
@Service
public class AdminPostingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminPostingService.class);

    private final ApplicationPostingRepository postingRepository;
    private final UserAccountRepository userAccountRepository;
    private final PostingVisibilityPolicy postingVisibilityPolicy;
    private final PostingMapper postingMapper;

    public AdminPostingService(
            ApplicationPostingRepository postingRepository,
            UserAccountRepository userAccountRepository,
            PostingVisibilityPolicy postingVisibilityPolicy,
            PostingMapper postingMapper
    ) {
        this.postingRepository = postingRepository;
        this.userAccountRepository = userAccountRepository;
        this.postingVisibilityPolicy = postingVisibilityPolicy;
        this.postingMapper = postingMapper;
    }

    @Transactional
    public PostingResponse createDraft(Long adminId, AdminPostingUpsertRequest request) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ApplicationPosting posting = new ApplicationPosting();
        applyUpsert(request, posting);
        posting.setStatus(ApplicationPostingStatus.DRAFT);
        posting.setCreatedByAdmin(admin);
        ApplicationPosting saved = postingRepository.save(posting);
        LOGGER.info("Posting created: id={}, adminId={}, status={}, category={}, title={}",
                saved.getId(), adminId, saved.getStatus(), saved.getCategory(), saved.getTitle());
        return postingMapper.toResponse(saved);
    }

    @Transactional
    public PostingResponse update(Long postingId, AdminPostingUpsertRequest request) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        postingVisibilityPolicy.assertAdminEditable(posting);
        applyUpsert(request, posting);
        return postingMapper.toResponse(postingRepository.save(posting));
    }

    @Transactional
    public PostingResponse publish(Long postingId) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        posting.changeStatus(ApplicationPostingStatus.PUBLISHED);
        return postingMapper.toResponse(postingRepository.save(posting));
    }

    @Transactional
    public PostingResponse close(Long postingId) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        posting.changeStatus(ApplicationPostingStatus.CLOSED);
        return postingMapper.toResponse(postingRepository.save(posting));
    }

    @Transactional
    public PostingResponse reopen(Long postingId) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        posting.changeStatus(ApplicationPostingStatus.PUBLISHED);
        return postingMapper.toResponse(postingRepository.save(posting));
    }

    @Transactional(readOnly = true)
    public PostingListResponse list(ApplicationCategory category, ApplicationPostingStatus status) {
        List<ApplicationPosting> postings = resolveList(category, status);
        List<PostingResponse> mapped = new ArrayList<>();
        for (ApplicationPosting posting : postings) {
            mapped.add(postingMapper.toResponse(posting));
        }
        return new PostingListResponse(mapped);
    }

    private List<ApplicationPosting> resolveList(ApplicationCategory category, ApplicationPostingStatus status) {
        if (category != null && status != null) {
            return postingRepository.findByCategoryAndStatus(category, status);
        }
        if (category != null) {
            return postingRepository.findByCategory(category);
        }
        if (status != null) {
            return postingRepository.findByStatus(status);
        }
        return postingRepository.findAll();
    }

    private void applyUpsert(AdminPostingUpsertRequest request, ApplicationPosting posting) {
        posting.setCategory(request.category());
        posting.setTitle(request.title());
        posting.setDescription(request.description());
        posting.setProjectName(request.projectName());
        posting.setProjectDetails(request.projectDetails());
    }
}
