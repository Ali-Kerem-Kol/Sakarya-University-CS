package com.project.project.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.dto.ApiErrorResponse;

/**
 * Sends a 401 response when unauthorized access is attempted.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                "UNAUTHORIZED",
                "Unauthorized",
                resolveRequestId(request),
                null
        );
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
