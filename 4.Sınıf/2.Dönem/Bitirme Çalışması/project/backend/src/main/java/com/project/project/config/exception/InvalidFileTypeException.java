package com.project.project.config.exception;

/**
 * Indicates the uploaded file has an unsupported content type.
 */
public class InvalidFileTypeException extends RuntimeException {

    public InvalidFileTypeException(String message) {
        super(message);
    }
}
