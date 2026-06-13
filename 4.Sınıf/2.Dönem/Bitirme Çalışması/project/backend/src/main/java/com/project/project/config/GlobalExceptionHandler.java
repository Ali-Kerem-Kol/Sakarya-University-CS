package com.project.project.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.AvailabilityOverlapException;
import com.project.project.config.exception.CvRequiredException;
import com.project.project.config.exception.FileTooLargeException;
import com.project.project.config.exception.DuplicateApplicationException;
import com.project.project.config.exception.InvalidAvailabilityException;
import com.project.project.config.exception.InvalidApplicationException;
import com.project.project.config.exception.InvalidEmailDomainException;
import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.config.exception.InvalidStatusTransitionException;
import com.project.project.config.exception.InvalidFileTypeException;
import com.project.project.config.exception.MissingAvailabilityException;
import com.project.project.config.exception.MissingCvException;
import com.project.project.config.exception.MissingProfileException;
import com.project.project.config.exception.NoRecipientsException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.config.exception.UnauthorizedException;
import com.project.project.dto.ApiErrorResponse;

/**
 * Maps exceptions to standardized API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        String path = request.getRequestURI();
        String errorCode;
        if (path.contains("/notes")) {
            errorCode = "INVALID_NOTE";
        } else if (path.contains("/applications")) {
            errorCode = "INVALID_APPLICATION";
        } else {
            errorCode = "VALIDATION_ERROR";
        }
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                path,
                errorCode,
                "Validation failed",
                resolveRequestId(request),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        String code = "CONFLICT";
        if ("ALREADY_SUBMITTED".equalsIgnoreCase(ex.getMessage())) {
            code = "ALREADY_SUBMITTED";
        }
        return buildResponse(HttpStatus.CONFLICT, code, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateApplication(
            DuplicateApplicationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_APPLICATION", ex.getMessage(), request);
    }

    @ExceptionHandler(MissingCvException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingCv(
            MissingCvException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "MISSING_CV", ex.getMessage(), request);
    }

    @ExceptionHandler(MissingProfileException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingProfile(
            MissingProfileException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "MISSING_PROFILE", ex.getMessage(), request);
    }

    @ExceptionHandler(MissingAvailabilityException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingAvailability(
            MissingAvailabilityException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "MISSING_AVAILABILITY", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION", ex.getMessage(), request);
    }

    @ExceptionHandler(AvailabilityOverlapException.class)
    public ResponseEntity<ApiErrorResponse> handleAvailabilityOverlap(
            AvailabilityOverlapException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "AVAILABILITY_OVERLAP", ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidApplication(
            InvalidApplicationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_APPLICATION", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPagination(
            InvalidPaginationException ex,
            HttpServletRequest request
    ) {
        String requestId = resolveRequestId(request);
        log.warn(
                "Invalid pagination (requestId={}, path={}): {}",
                requestId,
                request.getRequestURI(),
                shorten(ex.getMessage())
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PAGINATION", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidAvailabilityException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAvailability(
            InvalidAvailabilityException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_AVAILABILITY", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFileType(
            InvalidFileTypeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "Only PDF is allowed", request);
    }

    @ExceptionHandler(CvRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleCvRequired(
            CvRequiredException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "CV_REQUIRED", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidEmailDomain(
            InvalidEmailDomainException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_DOMAIN", ex.getMessage(), request);
    }

    @ExceptionHandler(NoRecipientsException.class)
    public ResponseEntity<ApiErrorResponse> handleNoRecipients(
            NoRecipientsException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "NO_RECIPIENTS", ex.getMessage(), request);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ApiErrorResponse> handleFileTooLarge(
            FileTooLargeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", ex.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "File exceeds max allowed size", request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipartException(
            MultipartException ex,
            HttpServletRequest request
    ) {
        String requestId = resolveRequestId(request);
        log.warn(
                "Multipart request failed (requestId={}, path={}): {}",
                requestId,
                request.getRequestURI(),
                shorten(ex.getMessage())
        );
        String message = "Multipart upload failed";
        String code = "MULTIPART_ERROR";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("size")) {
            message = "File exceeds max allowed size";
            code = "FILE_TOO_LARGE";
            status = HttpStatus.PAYLOAD_TOO_LARGE;
        }
        return buildResponse(status, code, message, request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPart(
            MissingServletRequestPartException ex,
            HttpServletRequest request
    ) {
        String requestId = resolveRequestId(request);
        log.warn(
                "Missing multipart part (requestId={}, path={}, part={}): {}",
                requestId,
                request.getRequestURI(),
                ex.getRequestPartName(),
                shorten(ex.getMessage())
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_MULTIPART_PART", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String requestId = resolveRequestId(request);
        log.warn(
                "Type mismatch (requestId={}, path={}, param={}): {}",
                requestId,
                request.getRequestURI(),
                ex.getName(),
                shorten(ex.getMessage())
        );
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "TYPE_MISMATCH",
                "Invalid request parameter type: " + ex.getName(),
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        if (isMultipartDataJsonError(request)) {
            String requestId = resolveRequestId(request);
            ResponseEntity<ApiErrorResponse> registerFormatError = tryBuildRegisterFormatError(ex, request, requestId);
            if (registerFormatError != null) {
                return registerFormatError;
            }
            log.warn(
                    "Malformed multipart JSON for register (requestId={}, path={}): {}",
                    requestId,
                    request.getRequestURI(),
                    shorten(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage())
            );
            return buildResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MULTIPART_JSON",
                    "Malformed JSON in multipart field 'data'",
                    request
            );
        }
        if (isTaskSubmissionMultipartJsonError(request)) {
            String requestId = resolveRequestId(request);
            log.warn(
                    "Malformed multipart JSON for task submission (requestId={}, path={}): {}",
                    requestId,
                    request.getRequestURI(),
                    shorten(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage())
            );
            return buildResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MULTIPART_JSON",
                    "Malformed JSON in multipart field 'data'",
                    request
            );
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Malformed JSON request body", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        if (isMultipartDataJsonError(request)) {
            String requestId = resolveRequestId(request);
            log.warn(
                    "Invalid media type for register multipart data (requestId={}, path={}): {}",
                    requestId,
                    request.getRequestURI(),
                    shorten(ex.getMessage())
            );
            return buildResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MULTIPART_JSON",
                    "Malformed JSON in multipart field 'data'",
                    request
            );
        }
        if (isTaskSubmissionMultipartJsonError(request)) {
            String requestId = resolveRequestId(request);
            log.warn(
                    "Invalid media type for task submission multipart data (requestId={}, path={}): {}",
                    requestId,
                    request.getRequestURI(),
                    shorten(ex.getMessage())
            );
            return buildResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MULTIPART_JSON",
                    "Malformed JSON in multipart field 'data'",
                    request
            );
        }
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication failed", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(
            Exception ex,
            HttpServletRequest request
    ) {
        String requestId = resolveRequestId(request);
        log.error("Unhandled exception on {} (requestId={})", request.getRequestURI(), requestId, ex);
        String message = "Unexpected error (requestId=" + requestId + ")";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request
    ) {
        return buildResponse(status, errorCode, message, request, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                errorCode,
                message,
                resolveRequestId(request),
                fieldErrors
        );
        return ResponseEntity.status(status).body(response);
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object attr = request.getAttribute(RequestCorrelationFilter.REQUEST_ATTR);
        String requestId = attr != null ? attr.toString() : null;
        if (requestId == null || requestId.isBlank()) {
            requestId = request.getHeader(RequestCorrelationFilter.HEADER);
        }
        if (requestId == null || requestId.isBlank()) {
            requestId = java.util.UUID.randomUUID().toString();
        }
        return requestId;
    }

    private boolean isMultipartDataJsonError(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data")
                && request.getRequestURI() != null
                && request.getRequestURI().contains("/auth/register");
    }

    private boolean isTaskSubmissionMultipartJsonError(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data")
                && request.getRequestURI() != null
                && request.getRequestURI().contains("/me/task-assignments/")
                && request.getRequestURI().endsWith("/submit");
    }

    private ResponseEntity<ApiErrorResponse> tryBuildRegisterFormatError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request,
            String requestId
    ) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof InvalidFormatException invalidFormat) {
                String field = invalidFormat.getPath() != null && !invalidFormat.getPath().isEmpty()
                        ? invalidFormat.getPath().get(invalidFormat.getPath().size() - 1).getFieldName()
                        : null;
                if ("gpa".equals(field) || "classYear".equals(field)) {
                    String message = "gpa".equals(field)
                            ? "gpa must be numeric (comma and dot decimals are supported)"
                            : "classYear must be an integer between 1 and 8";
                    Map<String, String> fieldErrors = Map.of(field, message);
                    log.warn(
                            "Register field format error (requestId={}, field={}, value={}): {}",
                            requestId,
                            field,
                            invalidFormat.getValue(),
                            shorten(invalidFormat.getOriginalMessage())
                    );
                    return buildResponse(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_REGISTER_FIELD_FORMAT",
                            "Invalid format in multipart field 'data'",
                            request,
                            fieldErrors
                    );
                }
            }
            current = current.getCause();
        }
        return null;
    }

    private String shorten(String text) {
        if (text == null) {
            return "";
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        int maxLength = 200;
        if (compact.length() <= maxLength) {
            return compact;
        }
        return compact.substring(0, maxLength) + "...";
    }
}
