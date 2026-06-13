package com.project.project.config.exception;

/**
 * Indicates a requested status transition is not allowed.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
