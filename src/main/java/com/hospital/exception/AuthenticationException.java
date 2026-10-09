package com.hospital.exception;

/**
 * Exception thrown when user authentication fails.
 */
public class AuthenticationException extends HospitalException {
    public AuthenticationException(String message) {
        super(message);
    }
}
