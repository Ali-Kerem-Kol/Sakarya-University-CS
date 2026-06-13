package com.project.project.config.exception;

/**
 * Indicates the uploaded file exceeds the configured maximum size.
 */
public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(String message) {
        super(message);
    }
}
