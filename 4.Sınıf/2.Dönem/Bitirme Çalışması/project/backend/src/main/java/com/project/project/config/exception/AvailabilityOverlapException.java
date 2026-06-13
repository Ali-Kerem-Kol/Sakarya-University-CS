package com.project.project.config.exception;

/**
 * Indicates availability slots overlap with existing data.
 */
public class AvailabilityOverlapException extends RuntimeException {

    public AvailabilityOverlapException(String message) {
        super(message);
    }
}
