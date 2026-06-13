package com.project.project.config.exception;

/**
 * Indicates application input data is invalid.
 */
public class InvalidApplicationException extends RuntimeException {

    public InvalidApplicationException(String message) {
        super(message);
    }
}
