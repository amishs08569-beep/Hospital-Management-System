package com.hospital.exception;

/**
 * Exception thrown when a database or SQL operation fails.
 */
public class DatabaseException extends HospitalException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
