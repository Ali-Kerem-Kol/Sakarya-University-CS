package com.project.project.service.posting;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.posting.PostingListResponse;
import com.project.project.dto.posting.PostingResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.service.policy.PostingVisibilityPolicy;

/**
 * Public posting read service.
 */
@Service
public class PublicPostingService {

    private final ApplicationPostingRepository postingRepository;
    private final PostingVisibilityPolicy postingVisibilityPolicy;
    private final PostingMapper postingMapper;

    public PublicPostingService(
            ApplicationPostingRepository postingRepository,
            PostingVisibilityPolicy postingVisibilityPolicy,
            PostingMapper postingMapper
    ) {
        this.postingRepository = postingRepository;
        this.postingVisibilityPolicy = postingVisibilityPolicy;
        this.postingMapper = postingMapper;
    }

    @Transactional(readOnly = true)
    public PostingListResponse listPublished(ApplicationCategory category) {
        List<ApplicationPosting> postings;
        if (category == null) {
            postings = postingRepository.findByStatus(ApplicationPostingStatus.PUBLISHED);
        } else {
            postings = postingRepository.findByCategoryAndStatus(category, ApplicationPostingStatus.PUBLISHED);
        }
        List<PostingResponse> responses = new ArrayList<>();
        for (ApplicationPosting posting : postings) {
            responses.add(postingMapper.toResponse(posting));
        }
        return new PostingListResponse(responses);
    }

    @Transactional(readOnly = true)
    public PostingResponse getPublished(Long postingId) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        postingVisibilityPolicy.assertPublicReadable(posting);
        return postingMapper.toResponse(posting);
    }
}
