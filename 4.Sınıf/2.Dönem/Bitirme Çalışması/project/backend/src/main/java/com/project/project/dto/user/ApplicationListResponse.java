package com.project.project.dto.user;

import java.util.List;

/**
 * Wraps application list responses.
 */
public record ApplicationListResponse(List<ApplicationResponse> applications) {
}
