package com.project.project.service.policy;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.ApplicationPostingStatus;
import com.project.project.entity.UserAccount;

/**
 * Centralized visibility policy for postings and submissions.
 */
@Service
public class PostingVisibilityPolicy {

    public void assertPublicReadable(ApplicationPosting posting) {
        if (posting.getStatus() != ApplicationPostingStatus.PUBLISHED) {
            throw new AccessDeniedException("Posting is not publicly visible");
        }
    }

    public void assertSubmissionAllowed(ApplicationPosting posting) {
        if (posting.getStatus() != ApplicationPostingStatus.PUBLISHED) {
            throw new AccessDeniedException("Submission is not allowed for this posting status");
        }
    }

    public void assertAdminEditable(ApplicationPosting posting) {
        if (posting.getStatus() == ApplicationPostingStatus.CLOSED) {
            throw new AccessDeniedException("Closed postings cannot be edited");
        }
    }

    public void assertAttachmentMutable(ApplicationPosting posting) {
        if (posting.getStatus() == ApplicationPostingStatus.CLOSED) {
            throw new AccessDeniedException("Closed postings cannot mutate attachments");
        }
    }

    public boolean isAdmin(UserAccount userAccount) {
        return userAccount != null && userAccount.getRole() == com.project.project.entity.Role.ADMIN;
    }
}
