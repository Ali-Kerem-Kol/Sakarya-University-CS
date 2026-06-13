package com.project.project.config.exception;

/**
 * Indicates an authentication failure for protected resources.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
