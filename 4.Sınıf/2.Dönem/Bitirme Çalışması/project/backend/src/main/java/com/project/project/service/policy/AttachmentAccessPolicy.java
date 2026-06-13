package com.project.project.service.policy;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.PostingAttachment;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationSubmissionRepository;

/**
 * Centralized authorization policy for posting attachment downloads.
 */
@Service
public class AttachmentAccessPolicy {

    private final ApplicationSubmissionRepository submissionRepository;

    public AttachmentAccessPolicy(ApplicationSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public void assertPostingAttachmentMatch(Long pathPostingId, PostingAttachment attachment) {
        if (!attachment.getPosting().getId().equals(pathPostingId)) {
            throw new AccessDeniedException("Attachment does not belong to posting");
        }
    }

    public void assertDownloadAllowed(ApplicationPosting posting, UserAccount requester) {
        ApplicationPostingStatus status = posting.getStatus();
        if (status == ApplicationPostingStatus.PUBLISHED) {
            return;
        }
        if (isAdmin(requester)) {
            return;
        }
        if (status == ApplicationPostingStatus.DRAFT) {
            throw new AccessDeniedException("Draft attachments are admin-only");
        }
        if (status == ApplicationPostingStatus.CLOSED) {
            if (requester == null) {
                throw new AccessDeniedException("Authentication required");
            }
            boolean hasSubmission = submissionRepository.existsByPostingIdAndUserId(posting.getId(), requester.getId());
            if (!hasSubmission) {
                throw new AccessDeniedException("Attachment access denied");
            }
            return;
        }
        throw new AccessDeniedException("Attachment access denied");
    }

    private boolean isAdmin(UserAccount user) {
        return user != null && user.getRole() == com.project.project.entity.Role.ADMIN;
    }
}
