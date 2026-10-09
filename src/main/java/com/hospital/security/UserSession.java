package com.hospital.security;

import com.hospital.model.User;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Encapsulates the active authenticated session for a user.
 */
public class UserSession {
    private final User user;
    private final RolePermission.RoleType role;
    private final Set<RolePermission.Permission> permissions;
    private final LocalDateTime loginTime;
    private LocalDateTime lastAccessedTime;

    public UserSession(User user) {
        this.user = user;
        this.role = RolePermission.RoleType.fromString(user.getRoleName());
        this.permissions = RolePermission.getPermissions(this.role);
        this.loginTime = LocalDateTime.now();
        this.lastAccessedTime = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public Integer getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : "anonymous";
    }

    public RolePermission.RoleType getRole() {
        return role;
    }

    public String getRoleName() {
        return role != null ? role.name() : (user != null ? user.getRoleName() : "UNKNOWN");
    }

    public Set<RolePermission.Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(RolePermission.Permission permission) {
        return permissions != null && permissions.contains(permission);
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void updateAccessTime() {
        this.lastAccessedTime = LocalDateTime.now();
    }
}
