package com.project.project.dto.task;

/**
 * Attachment metadata for graph node.
 */
public record TaskGraphAttachmentResponse(
        Long id,
        String fileName,
        String url
) {
}
