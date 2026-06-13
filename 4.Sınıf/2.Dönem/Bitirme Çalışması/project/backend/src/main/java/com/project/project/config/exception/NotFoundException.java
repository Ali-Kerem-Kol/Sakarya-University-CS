package com.project.project.config.exception;

/**
 * Indicates a requested resource cannot be found.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
