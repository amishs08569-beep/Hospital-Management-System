package com.hospital.security;

import com.hospital.exception.AuthorizationException;

/**
 * Manages the current thread and application-wide security context.
 */
public class SecurityContext {

    private static final ThreadLocal<UserSession> threadLocalSession = new ThreadLocal<>();

    private static volatile UserSession globalSession;

    private SecurityContext() {
        // Utility class; prevent instantiation.
    }

    public static void setCurrentSession(UserSession session) {
        threadLocalSession.set(session);
        globalSession = session;
    }

    public static void setThreadSession(UserSession session) {
        threadLocalSession.set(session);
    }

    public static UserSession getCurrentSession() {
        UserSession session = threadLocalSession.get();
        return session != null ? session : globalSession;
    }

    public static boolean isAuthenticated() {
        return getCurrentSession() != null;
    }

    public static void clear() {
        threadLocalSession.remove();
        globalSession = null;
    }

    public static void clearThread() {
        threadLocalSession.remove();
    }

    /**
     * Asserts that a user is authenticated and has the required permission.
     *
     * @param permission The required permission
     * @throws AuthorizationException if authentication or authorization fails
     */
    public static void requirePermission(
            RolePermission.Permission permission) {

        UserSession currentSession = getCurrentSession();

        if (currentSession == null) {
            throw new AuthorizationException(
                    "Authentication required. Please log in first.");
        }

        if (!currentSession.hasPermission(permission)) {
            throw new AuthorizationException(
                    "Access denied: missing permission [" + permission
                            + "] for role [" + currentSession.getRoleName()
                            + "]");
        }
    }

    /**
     * Checks whether the current user has the specified permission.
     */
    public static boolean hasPermission(
            RolePermission.Permission permission) {

        UserSession currentSession = getCurrentSession();

        return currentSession != null
                && currentSession.hasPermission(permission);
    }

    public static Integer getCurrentUserId() {
        UserSession currentSession = getCurrentSession();

        return currentSession != null
                ? currentSession.getUserId()
                : null;
    }

    public static String getCurrentUsername() {
        UserSession currentSession = getCurrentSession();

        return currentSession != null
                ? currentSession.getUsername()
                : "SYSTEM";
    }
}
