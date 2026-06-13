package com.project.project.dto.task;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Graph branch metadata.
 */
public record TaskGraphBranchResponse(
        String branchKey,
        String branchName,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long ownerUserId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String ownerUserColor
) {
}
