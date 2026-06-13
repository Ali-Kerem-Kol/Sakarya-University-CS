package com.project.project.config.exception;

/**
 * Indicates a user profile is required before submitting an application.
 */
public class MissingProfileException extends RuntimeException {

    public MissingProfileException(String message) {
        super(message);
    }
}
