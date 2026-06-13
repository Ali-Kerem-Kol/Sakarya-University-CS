package com.project.project.config.exception;

/**
 * Indicates a user must upload a CV before submitting an application.
 */
public class MissingCvException extends RuntimeException {

    public MissingCvException(String message) {
        super(message);
    }
}
