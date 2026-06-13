package com.project.project.config.exception;

/**
 * Indicates availability slots are required before submitting an application.
 */
public class MissingAvailabilityException extends RuntimeException {

    public MissingAvailabilityException(String message) {
        super(message);
    }
}
