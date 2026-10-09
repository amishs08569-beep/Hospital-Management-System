package com.hospital.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testPasswordHashingAndVerification() {
        String password = "admin123";
        String hash = PasswordUtil.hashPassword(password);
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtil.checkPassword("admin123", hash));
        assertFalse(PasswordUtil.checkPassword("wrongpassword", hash));
        System.out.println("HASH_ADMIN: " + PasswordUtil.hashPassword("admin123"));
        System.out.println("HASH_DOCTOR: " + PasswordUtil.hashPassword("doctor123"));
        System.out.println("HASH_RECEP: " + PasswordUtil.hashPassword("recep123"));
        System.out.println("HASH_PHARM: " + PasswordUtil.hashPassword("pharm123"));
        System.out.println("HASH_LAB: " + PasswordUtil.hashPassword("lab123"));
        System.out.println("HASH_BILL: " + PasswordUtil.hashPassword("bill123"));
    }
}
