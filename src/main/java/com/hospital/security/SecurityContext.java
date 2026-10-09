package com.hospital.security;

import com.hospital.exception.AuthorizationException;

/**
 * Manages the current thread and application-wide security context.
 */
public class SecurityContext {

    private static volatile UserSession currentSession;

    private SecurityContext() {}

    public static void setCurrentSession(UserSession session) {
        currentSession = session;
    }

    public static UserSession getCurrentSession() {
        return currentSession;
    }

    public static boolean isAuthenticated() {
        return currentSession != null;
    }

    public static void clear() {
        currentSession = null;
    }

    /**
     * Asserts that a user is currently authenticated and has the required permission.
     * Throws an AuthorizationException if the assertion fails.
     *
     * @param permission The required permission
     */
    public static void requirePermission(RolePermission.Permission permission) {
        if (!isAuthenticated()) {
            throw new AuthorizationException("Authentication required. Please log in first.");
        }
        if (!currentSession.hasPermission(permission)) {
            throw new AuthorizationException("Access denied: missing permission [" + permission + "] for role [" + currentSession.getRoleName() + "]");
        }
    }

    /**
     * Checks if current user has the specified permission without throwing an exception.
     */
    public static boolean hasPermission(RolePermission.Permission permission) {
        return isAuthenticated() && currentSession.hasPermission(permission);
    }

    public static Integer getCurrentUserId() {
        return currentSession != null ? currentSession.getUserId() : null;
    }

    public static String getCurrentUsername() {
        return currentSession != null ? currentSession.getUsername() : "SYSTEM";
    }
}
