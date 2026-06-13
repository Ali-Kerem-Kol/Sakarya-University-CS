package com.project.project.config.exception;

/**
 * Raised when CV file is required but missing.
 */
public class CvRequiredException extends BadRequestException {

    public CvRequiredException(String message) {
        super(message);
    }
}
