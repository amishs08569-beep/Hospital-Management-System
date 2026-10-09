package com.hospital.service;

import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.UserSession;

import java.util.List;

public interface AuthService {
    UserSession login(String username, String password);
    void logout();
    boolean changePassword(int userId, String oldPassword, String newPassword);
    User createUser(User user, String plainPassword);
    boolean updateUser(User user);
    boolean setUserActive(int userId, boolean active);
    List<User> getAllUsers();
    List<Role> getAllRoles();
}
