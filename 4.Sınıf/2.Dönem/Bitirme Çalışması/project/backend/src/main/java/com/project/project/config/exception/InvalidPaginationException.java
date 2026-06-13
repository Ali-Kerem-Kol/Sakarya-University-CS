package com.project.project.config.exception;

/**
 * Indicates pagination or sorting parameters are invalid.
 */
public class InvalidPaginationException extends RuntimeException {

    public InvalidPaginationException(String message) {
        super(message);
    }
}
