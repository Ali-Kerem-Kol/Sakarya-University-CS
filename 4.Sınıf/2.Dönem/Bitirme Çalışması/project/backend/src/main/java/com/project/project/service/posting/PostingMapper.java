package com.project.project.service.posting;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.project.project.dto.posting.AttachmentSummaryResponse;
import com.project.project.dto.posting.PostingResponse;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.PostingAttachment;

/**
 * Maps posting entities to API DTOs.
 */
@Component
public class PostingMapper {

    public PostingResponse toResponse(ApplicationPosting posting) {
        List<AttachmentSummaryResponse> attachments = new ArrayList<>();
        for (PostingAttachment attachment : posting.getAttachments()) {
            attachments.add(new AttachmentSummaryResponse(
                    attachment.getId(),
                    attachment.getDocument().getOriginalFileName(),
                    attachment.getDocument().getContentType(),
                    attachment.getDocument().getSize(),
                    "/postings/" + posting.getId() + "/attachments/" + attachment.getId() + "/download"
            ));
        }
        return new PostingResponse(
                posting.getId(),
                posting.getCategory(),
                posting.getTitle(),
                posting.getDescription(),
                posting.getProjectName(),
                posting.getProjectDetails(),
                posting.getStatus(),
                posting.getPublishedAt(),
                posting.getClosedAt(),
                posting.getCreatedAt(),
                attachments
        );
    }
}
