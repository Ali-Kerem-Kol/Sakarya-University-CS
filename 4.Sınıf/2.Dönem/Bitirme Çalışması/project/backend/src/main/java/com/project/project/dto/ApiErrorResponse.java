package com.project.project.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a standardized error payload for API responses.
 */
public record ApiErrorResponse(
        Instant timestamp,
        String path,
        String errorCode,
        String message,
        String requestId,
        Map<String, String> fieldErrors
) {
    @JsonProperty("code")
    public String code() {
        return errorCode;
    }
}
