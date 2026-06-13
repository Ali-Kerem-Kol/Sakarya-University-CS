package com.project.project.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.posting.PostingAttachmentService;
import com.project.project.service.policy.PublicDownloadRateLimitHook;

/**
 * Secure download endpoint for posting attachments.
 */
@RestController
@RequestMapping("/api/v1/postings")
public class PostingAttachmentDownloadController {

    private final PostingAttachmentService postingAttachmentService;
    private final PublicDownloadRateLimitHook publicDownloadRateLimitHook;

    public PostingAttachmentDownloadController(
            PostingAttachmentService postingAttachmentService,
            PublicDownloadRateLimitHook publicDownloadRateLimitHook
    ) {
        this.postingAttachmentService = postingAttachmentService;
        this.publicDownloadRateLimitHook = publicDownloadRateLimitHook;
    }

    @GetMapping("/{postingId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long postingId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        Long requesterId = principal != null ? principal.getUserAccount().getId() : null;
        String requesterKey = requesterId != null ? "user:" + requesterId : "public";
        publicDownloadRateLimitHook.check(requesterKey, postingId, attachmentId);
        PostingAttachmentService.AttachmentDownload download =
                postingAttachmentService.download(postingId, attachmentId, requesterId);
        String contentDisposition = download.contentType() != null
                && download.contentType().toLowerCase(java.util.Locale.ROOT).startsWith("application/pdf")
                ? "inline"
                : "attachment";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition + "; filename=\"" + download.originalFileName() + "\"")
                .body(download.resource());
    }
}
