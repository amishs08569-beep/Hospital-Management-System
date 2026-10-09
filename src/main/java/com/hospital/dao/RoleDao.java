package com.hospital.dao;

import com.hospital.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleDao {
    List<Role> findAll();
    Optional<Role> findById(int roleId);
    Optional<Role> findByName(String roleName);
}
