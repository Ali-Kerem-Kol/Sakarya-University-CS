package com.project.project.config.exception;

/**
 * Indicates availability slot input data is invalid.
 */
public class InvalidAvailabilityException extends RuntimeException {

    public InvalidAvailabilityException(String message) {
        super(message);
    }
}
