package com.project.project.service.policy;

import org.springframework.stereotype.Service;

/**
 * Placeholder hook for public attachment download rate limiting.
 */
@Service
public class PublicDownloadRateLimitHook {

    public void check(String requesterKey, Long postingId, Long attachmentId) {
        // Intentionally no-op. Replace with real limiter integration.
    }
}
