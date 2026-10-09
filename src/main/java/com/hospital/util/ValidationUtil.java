package com.hospital.util;

import com.hospital.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Validates domain objects, form inputs, and parameter constraints.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9\\-\\s()]{7,25}$");

    private ValidationUtil() {}

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be blank.");
        }
    }

    public static void validateEmail(String email, String fieldName) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new ValidationException("Invalid email format for " + fieldName + ": " + email);
            }
        }
    }

    public static void validatePhone(String phone, String fieldName) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new ValidationException("Invalid phone number format for " + fieldName + ": " + phone);
            }
        }
    }

    public static void requirePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be a positive number.");
        }
    }

    public static void requireNonNegative(Number value, String fieldName) {
        if (value == null || value.doubleValue() < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }
}
