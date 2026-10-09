package com.hospital.exception;

/**
 * Exception thrown when a state conflict occurs (e.g., overlapping appointment, bed already occupied, duplicate entry).
 */
public class ConflictException extends HospitalException {
    public ConflictException(String message) {
        super(message);
    }
}
