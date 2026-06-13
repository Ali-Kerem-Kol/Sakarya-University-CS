package com.project.project.dto.task;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.project.entity.TimelineEventType;

/**
 * Timeline event payload for admin UI.
 */
public record TaskTimelineEventResponse(
        TimelineEventType eventType,
        Instant createdAt,
        TaskTimelineActorResponse actor,
        TaskTimelineTaskResponse task,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        TaskTimelineAssignmentResponse assignment,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        TaskTimelineStatsResponse stats
) {
}
