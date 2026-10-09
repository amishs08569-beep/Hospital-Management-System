package com.hospital.exception;

/**
 * Exception thrown when input data validation fails.
 */
public class ValidationException extends HospitalException {
    public ValidationException(String message) {
        super(message);
    }
}
