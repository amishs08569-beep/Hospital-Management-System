package com.hospital.exception;

/**
 * Exception thrown when a requested entity cannot be found.
 */
public class ResourceNotFoundException extends HospitalException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
