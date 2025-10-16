package com.example.Backend.service;

import com.example.Backend.entity.SuperAdmin;

import java.util.List;
import java.util.UUID;

public interface SuperAdminService {

    /**
     * Get super admin by ID
     * 
     * @param superAdminId Super admin ID
     * @return SuperAdmin entity
     */
    SuperAdmin getSuperAdminById(UUID superAdminId);

    /**
     * Get super admin by username
     * 
     * @param username Super admin username
     * @return SuperAdmin entity
     */
    SuperAdmin getSuperAdminByUsername(String username);

    /**
     * Get all super admins
     * 
     * @return List of super admins
     */
    List<SuperAdmin> getAllSuperAdmins();

    /**
     * Get active super admins
     * 
     * @return List of active super admins
     */
    List<SuperAdmin> getActiveSuperAdmins();
}
