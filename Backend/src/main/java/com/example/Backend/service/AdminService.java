package com.example.Backend.service;

import com.example.Backend.dto.AdminDTO;
import com.example.Backend.entity.Admin;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    /**
     * Create admin account
     * 
     * @param adminDTO Admin data
     * @return Created admin
     */
    Admin createAdmin(AdminDTO adminDTO);

    /**
     * Get admin by ID
     * 
     * @param adminId Admin ID
     * @return Admin entity
     */
    Admin getAdminById(UUID adminId);

    /**
     * Get admin by username
     * 
     * @param username Admin username
     * @return Admin entity
     */
    Admin getAdminByUsername(String username);

    /**
     * Get all admins
     * 
     * @return List of admins
     */
    List<Admin> getAllAdmins();

    /**
     * Update admin
     * 
     * @param adminId  Admin ID
     * @param adminDTO Updated admin data
     * @return Updated admin
     */
    Admin updateAdmin(UUID adminId, AdminDTO adminDTO);

    /**
     * Delete admin
     * 
     * @param adminId Admin ID
     */
    void deleteAdmin(UUID adminId);

    /**
     * Get active admins
     * 
     * @return List of active admins
     */
    List<Admin> getActiveAdmins();

    /**
     * Activate admin account
     * 
     * @param adminId Admin ID
     * @return Updated admin
     */
    Admin activateAdmin(UUID adminId);

    /**
     * Deactivate admin account
     * 
     * @param adminId Admin ID
     * @return Updated admin
     */
    Admin deactivateAdmin(UUID adminId);
}
