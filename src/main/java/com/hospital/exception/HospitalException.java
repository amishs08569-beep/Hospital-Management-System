package com.hospital.exception;

/**
 * Base unchecked exception for Hospital Management System application errors.
 */
public class HospitalException extends RuntimeException {
    public HospitalException(String message) {
        super(message);
    }

    public HospitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
