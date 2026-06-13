package com.project.project.config.exception;

/**
 * Indicates a conflict with the current state of a resource.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
