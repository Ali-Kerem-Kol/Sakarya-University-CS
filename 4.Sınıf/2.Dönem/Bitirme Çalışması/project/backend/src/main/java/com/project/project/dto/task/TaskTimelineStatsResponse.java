package com.project.project.dto.task;

/**
 * Project-level assignment counters at event creation time.
 */
public record TaskTimelineStatsResponse(
        long assigned,
        long approved,
        long rejected,
        long pending
) {
}
