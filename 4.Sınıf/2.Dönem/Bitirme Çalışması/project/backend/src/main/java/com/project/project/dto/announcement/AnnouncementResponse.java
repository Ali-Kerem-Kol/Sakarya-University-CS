package com.project.project.dto.announcement;

import java.time.Instant;

/**
 * Announcement payload.
 */
public record AnnouncementResponse(
        Long id,
        String title,
        String content,
        Instant createdAt
) {
}

