package com.project.project.config.exception;

/**
 * Indicates a user already has an active application for the same position.
 */
public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }
}
