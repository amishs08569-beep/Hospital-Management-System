package com.hospital.exception;

/**
 * Exception thrown when an authenticated user attempts an unauthorized operation.
 */
public class AuthorizationException extends HospitalException {
    public AuthorizationException(String message) {
        super(message);
    }
}
