package com.hospital.service.impl;

import com.hospital.dao.RoleDao;
import com.hospital.dao.UserDao;
import com.hospital.dao.impl.RoleDaoImpl;
import com.hospital.dao.impl.UserDaoImpl;
import com.hospital.exception.AuthenticationException;
import com.hospital.exception.ConflictException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.PasswordUtil;
import com.hospital.security.RolePermission;
import com.hospital.security.SecurityContext;
import com.hospital.security.UserSession;
import com.hospital.service.AuditService;
import com.hospital.service.AuthService;
import com.hospital.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final AuditService auditService;

    public AuthServiceImpl() {
        this.userDao = new UserDaoImpl();
        this.roleDao = new RoleDaoImpl();
        this.auditService = new AuditServiceImpl();
    }

    public AuthServiceImpl(UserDao userDao, RoleDao roleDao, AuditService auditService) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.auditService = auditService;
    }

    @Override
    public UserSession login(String username, String password) {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireNonBlank(password, "Password");

        Optional<User> userOpt = userDao.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User '{}' not found", username);
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Authentication blocked: User '{}' is deactivated", username);
            throw new AuthenticationException("Your account has been deactivated. Please contact an administrator.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: Incorrect password for user '{}'", username);
            throw new AuthenticationException("Invalid username or password.");
        }

        UserSession session = new UserSession(user);
        SecurityContext.setCurrentSession(session);
        auditService.log("LOGIN", "USER", String.valueOf(user.getUserId()), "Successful login by " + user.getUsername());
        logger.info("User '{}' logged in successfully with role [{}]", user.getUsername(), user.getRoleName());
        return session;
    }

    @Override
    public void logout() {
        if (SecurityContext.isAuthenticated()) {
            String username = SecurityContext.getCurrentUsername();
            Integer uid = SecurityContext.getCurrentUserId();
            auditService.log("LOGOUT", "USER", String.valueOf(uid), "User logged out");
            SecurityContext.clear();
            logger.info("User '{}' logged out.", username);
        }
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        ValidationUtil.requireNonBlank(oldPassword, "Old Password");
        ValidationUtil.requireNonBlank(newPassword, "New Password");

        if (newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long.");
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password does not match.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean success = userDao.updatePassword(userId, newHash);
        if (success) {
            auditService.log("CHANGE_PASSWORD", "USER", String.valueOf(userId), "User changed password");
        }
        return success;
    }

    @Override
    public User createUser(User user, String plainPassword) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_USERS);

        ValidationUtil.requireNonBlank(user.getUsername(), "Username");
        ValidationUtil.requireNonBlank(user.getFullName(), "Full Name");
        ValidationUtil.requireNonBlank(user.getEmail(), "Email");
        ValidationUtil.validateEmail(user.getEmail(), "Email");
        ValidationUtil.requireNonBlank(plainPassword, "Password");

        if (plainPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }

        if (userDao.findByUsername(user.getUsername().trim()).isPresent()) {
            throw new ConflictException("Username '" + user.getUsername() + "' is already taken.");
        }
        if (userDao.findByEmail(user.getEmail().trim()).isPresent()) {
            throw new ConflictException("Email '" + user.getEmail() + "' is already registered.");
        }

        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        int id = userDao.insert(user);
        user.setUserId(id);

        auditService.log("CREATE", "USER", String.valueOf(id), "Created new user " + user.getUsername() + " with role " + user.getRoleName());
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_USERS);
        ValidationUtil.requireNonBlank(user.getFullName(), "Full Name");
        ValidationUtil.requireNonBlank(user.getEmail(), "Email");
        ValidationUtil.validateEmail(user.getEmail(), "Email");

        boolean updated = userDao.update(user);
        if (updated) {
            auditService.log("UPDATE", "USER", String.valueOf(user.getUserId()), "Updated user " + user.getUsername());
        }
        return updated;
    }

    @Override
    public boolean setUserActive(int userId, boolean active) {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_USERS);
        boolean updated = userDao.setActiveStatus(userId, active);
        if (updated) {
            auditService.log("STATUS_CHANGE", "USER", String.valueOf(userId), "Set user active to " + active);
        }
        return updated;
    }

    @Override
    public List<User> getAllUsers() {
        SecurityContext.requirePermission(RolePermission.Permission.MANAGE_USERS);
        return userDao.findAll();
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDao.findAll();
    }
}
