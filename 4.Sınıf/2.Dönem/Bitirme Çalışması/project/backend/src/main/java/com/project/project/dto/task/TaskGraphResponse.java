package com.project.project.dto.task;

import java.util.List;

/**
 * Graph-ready task timeline payload.
 */
public record TaskGraphResponse(
        Long projectId,
        List<TaskGraphBranchResponse> branches,
        List<TaskGraphNodeResponse> nodes,
        List<TaskGraphEdgeResponse> edges
) {
}
