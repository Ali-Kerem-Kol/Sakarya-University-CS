package com.project.project.service.posting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.posting.AttachmentSummaryResponse;
import com.project.project.entity.ApplicationPosting;
import com.project.project.entity.Document;
import com.project.project.entity.PostingAttachment;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.DocumentRepository;
import com.project.project.repository.PostingAttachmentRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.policy.AttachmentAccessPolicy;
import com.project.project.service.policy.PostingVisibilityPolicy;
import com.project.project.service.storage.DocumentType;
import com.project.project.service.storage.FileStorageResult;
import com.project.project.service.storage.FileStorageService;

/**
 * Attachment management and secure download service.
 */
@Service
public class PostingAttachmentService {

    private final ApplicationPostingRepository postingRepository;
    private final PostingAttachmentRepository attachmentRepository;
    private final DocumentRepository documentRepository;
    private final UserAccountRepository userAccountRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentAccessPolicy attachmentAccessPolicy;
    private final PostingVisibilityPolicy postingVisibilityPolicy;

    public PostingAttachmentService(
            ApplicationPostingRepository postingRepository,
            PostingAttachmentRepository attachmentRepository,
            DocumentRepository documentRepository,
            UserAccountRepository userAccountRepository,
            FileStorageService fileStorageService,
            AttachmentAccessPolicy attachmentAccessPolicy,
            PostingVisibilityPolicy postingVisibilityPolicy
    ) {
        this.postingRepository = postingRepository;
        this.attachmentRepository = attachmentRepository;
        this.documentRepository = documentRepository;
        this.userAccountRepository = userAccountRepository;
        this.fileStorageService = fileStorageService;
        this.attachmentAccessPolicy = attachmentAccessPolicy;
        this.postingVisibilityPolicy = postingVisibilityPolicy;
    }

    @Transactional
    public List<AttachmentSummaryResponse> upload(Long adminId, Long postingId, List<MultipartFile> files) {
        ApplicationPosting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new NotFoundException("Posting not found"));
        postingVisibilityPolicy.assertAttachmentMutable(posting);
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<AttachmentSummaryResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            FileStorageResult result = fileStorageService.storeFile(adminId, DocumentType.POSTING_ATTACHMENT, file);
            Document document = new Document();
            document.setOriginalFileName(result.originalFileName());
            document.setContentType(result.contentType());
            document.setSize(result.size());
            document.setStorageKey(result.storageKey());
            document.setChecksum(result.checksum());
            document.setUploadedBy(admin);
            document.setUploadedAt(Instant.now());
            Document savedDocument = documentRepository.save(document);

            PostingAttachment attachment = new PostingAttachment();
            attachment.setPosting(posting);
            attachment.setDocument(savedDocument);
            PostingAttachment savedAttachment = attachmentRepository.save(attachment);
            responses.add(toResponse(savedAttachment));
        }
        return responses;
    }

    @Transactional
    public void delete(Long postingId, Long attachmentId) {
        PostingAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        if (!attachment.getPosting().getId().equals(postingId)) {
            throw new AccessDeniedException("Attachment does not belong to posting");
        }
        postingVisibilityPolicy.assertAttachmentMutable(attachment.getPosting());
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public AttachmentDownload download(Long postingId, Long attachmentId, Long requesterIdOrNull) {
        PostingAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        attachmentAccessPolicy.assertPostingAttachmentMatch(postingId, attachment);

        UserAccount requester = null;
        if (requesterIdOrNull != null) {
            requester = userAccountRepository.findById(requesterIdOrNull)
                    .orElseThrow(() -> new NotFoundException("User not found"));
        }
        attachmentAccessPolicy.assertDownloadAllowed(attachment.getPosting(), requester);
        Resource resource = fileStorageService.loadFileAsResource(attachment.getDocument().getStorageKey());
        return new AttachmentDownload(resource, attachment.getDocument().getContentType(),
                attachment.getDocument().getOriginalFileName());
    }

    private AttachmentSummaryResponse toResponse(PostingAttachment attachment) {
        return new AttachmentSummaryResponse(
                attachment.getId(),
                attachment.getDocument().getOriginalFileName(),
                attachment.getDocument().getContentType(),
                attachment.getDocument().getSize(),
                "/postings/" + attachment.getPosting().getId() + "/attachments/" + attachment.getId() + "/download"
        );
    }

    /**
     * Download descriptor for attachment resources.
     */
    public record AttachmentDownload(Resource resource, String contentType, String originalFileName) {
    }
}
