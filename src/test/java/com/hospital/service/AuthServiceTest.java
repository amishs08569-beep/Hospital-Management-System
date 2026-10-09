package com.hospital.service;

import com.hospital.config.DatabaseConfig;
import com.hospital.exception.AuthenticationException;
import com.hospital.security.RolePermission;
import com.hospital.security.UserSession;
import com.hospital.service.impl.AuthServiceImpl;
import com.hospital.util.DbInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private static AuthService authService;

    @BeforeAll
    public static void setup() {
        // Initializes embedded in-memory database with full schema and seed data
        DatabaseConfig.initializeH2();
        DbInitializer.initializeDatabase();
        authService = new AuthServiceImpl();
    }

    @Test
    public void testSuccessfulAdminLogin() {
        UserSession session = authService.login("admin", "admin123");
        assertNotNull(session);
        assertEquals("admin", session.getUsername());
        assertEquals(RolePermission.RoleType.ADMIN, session.getRole());
        assertTrue(session.hasPermission(RolePermission.Permission.MANAGE_USERS));
        assertTrue(session.hasPermission(RolePermission.Permission.MANAGE_DOCTORS));
        assertTrue(session.hasPermission(RolePermission.Permission.VIEW_SYSTEM_REPORTS));
    }

    @Test
    public void testDoctorLoginAndRolePermissions() {
        UserSession session = authService.login("dr.smith", "doctor123");
        assertNotNull(session);
        assertEquals(RolePermission.RoleType.DOCTOR, session.getRole());
        assertTrue(session.hasPermission(RolePermission.Permission.CREATE_MEDICAL_RECORD));
        assertTrue(session.hasPermission(RolePermission.Permission.CREATE_PRESCRIPTION));
        assertFalse(session.hasPermission(RolePermission.Permission.MANAGE_USERS));
    }

    @Test
    public void testInvalidPasswordThrowsException() {
        assertThrows(AuthenticationException.class, () -> {
            authService.login("admin", "wrong_password");
        });
    }

    @Test
    public void testNonExistentUserThrowsException() {
        assertThrows(AuthenticationException.class, () -> {
            authService.login("non_existent_user_999", "password123");
        });
    }
}
