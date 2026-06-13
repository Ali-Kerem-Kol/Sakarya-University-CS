package com.project.project.dto.user;

import java.util.List;

/**
 * Groups user documents by category for list responses.
 */
public record UserDocumentsResponse(
        List<DocumentResponse> cvDocuments
) {
}
