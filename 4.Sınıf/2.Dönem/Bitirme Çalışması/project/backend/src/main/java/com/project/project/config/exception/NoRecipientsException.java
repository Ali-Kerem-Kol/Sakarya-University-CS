package com.project.project.config.exception;

/**
 * Raised when a mail job has no eligible recipients.
 */
public class NoRecipientsException extends BadRequestException {

    public NoRecipientsException(String message) {
        super(message);
    }
}
