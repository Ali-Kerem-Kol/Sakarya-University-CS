package com.project.project.config.exception;

/**
 * Raised when a registration email does not match the required student domain.
 */
public class InvalidEmailDomainException extends BadRequestException {

    public InvalidEmailDomainException(String message) {
        super(message);
    }
}
