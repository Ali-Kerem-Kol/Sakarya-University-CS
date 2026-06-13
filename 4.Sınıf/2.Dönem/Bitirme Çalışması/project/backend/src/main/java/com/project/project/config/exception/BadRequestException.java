package com.project.project.config.exception;

/**
 * Indicates the client sent an invalid request payload.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
