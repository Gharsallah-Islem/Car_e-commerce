package com.example.Backend.service;

import com.example.Backend.dto.AdminDTO;
import com.example.Backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    /**
     * Create admin account
     * 
     * @param adminDTO Admin data
     * @return Created admin user
     */
    User createAdmin(AdminDTO adminDTO);

    /**
     * Get admin by ID
     * 
     * @param adminId Admin ID
     * @return Admin user entity
     */
    User getAdminById(UUID adminId);

    /**
     * Get admin by username
     * 
     * @param username Admin username
     * @return Admin user entity
     */
    User getAdminByUsername(String username);

    /**
     * Get all admins
     * 
     * @return List of admin users
     */
    List<User> getAllAdmins();

    /**
     * Update admin
     * 
     * @param adminId  Admin ID
     * @param adminDTO Updated admin data
     * @return Updated admin user
     */
    User updateAdmin(UUID adminId, AdminDTO adminDTO);

    /**
     * Delete admin
     * 
     * @param adminId Admin ID
     */
    void deleteAdmin(UUID adminId);

    /**
     * Get active admins
     * 
     * @return List of active admin users
     */
    List<User> getActiveAdmins();

    /**
     * Activate user account (Generic)
     * 
     * @param userId User ID
     * @return Updated user
     */
    User activateUser(UUID userId);

    /**
     * Deactivate user account (Generic)
     * 
     * @param userId User ID
     * @return Updated user
     */
    User deactivateUser(UUID userId);
}
