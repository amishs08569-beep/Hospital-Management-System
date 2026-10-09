package com.hospital.dao;

import com.hospital.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    int insert(User user);
    boolean update(User user);
    boolean updatePassword(int userId, String passwordHash);
    boolean setActiveStatus(int userId, boolean active);
}
