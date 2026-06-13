package com.project.project.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.dto.ApiErrorResponse;

/**
 * Sends a standardized JSON response for access denied errors.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                "FORBIDDEN",
                "Access denied",
                resolveRequestId(request),
                null
        );
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), error);
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object attr = request.getAttribute(com.project.project.config.RequestCorrelationFilter.REQUEST_ATTR);
        String requestId = attr != null ? attr.toString() : null;
        if (requestId == null || requestId.isBlank()) {
            requestId = request.getHeader(com.project.project.config.RequestCorrelationFilter.HEADER);
        }
        if (requestId == null || requestId.isBlank()) {
            requestId = java.util.UUID.randomUUID().toString();
        }
        return requestId;
    }
}
