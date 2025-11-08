package com.example.Backend.service;

import com.example.Backend.dto.UserDTO;
import com.example.Backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Create a new user
     * 
     * @param userDTO User data
     * @return Created user
     */
    User createUser(UserDTO userDTO);

    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User entity
     */
    User getUserById(UUID id);

    /**
     * Get user by username
     * 
     * @param username Username
     * @return User entity
     */
    User getUserByUsername(String username);

    /**
     * Get user by email
     * 
     * @param email Email address
     * @return User entity
     */
    User getUserByEmail(String email);

    /**
     * Update user information
     * 
     * @param id      User ID
     * @param userDTO Updated user data
     * @return Updated user
     */
    User updateUser(UUID id, UserDTO userDTO);

    /**
     * Delete user by ID
     * 
     * @param id User ID
     */
    void deleteUser(UUID id);

    /**
     * Get all users
     * 
     * @return List of all users
     */
    List<User> getAllUsers();

    /**
     * Search users by term (username, email, full name)
     * 
     * @param searchTerm Search term
     * @return List of matching users
     */
    List<User> searchUsers(String searchTerm);

    /**
     * Get users by role
     * 
     * @param roleName Role name
     * @return List of users with the role
     */
    List<User> getUsersByRole(String roleName);

    /**
     * Get active users (with recent orders)
     * 
     * @param since Date from which to consider activity
     * @return List of active users
     */
    List<User> getActiveUsers(LocalDateTime since);

    /**
     * Check if username exists
     * 
     * @param username Username to check
     * @return true if exists
     */
    boolean usernameExists(String username);

    /**
     * Check if email exists
     * 
     * @param email Email to check
     * @return true if exists
     */
    boolean emailExists(String email);

    /**
     * Count users by role
     * 
     * @param roleName Role name
     * @return Count of users
     */
    Long countUsersByRole(String roleName);

    /**
     * Generate and send email verification code
     * 
     * @param email User email
     * @return Generated verification code (for testing purposes)
     */
    String sendEmailVerification(String email);

    /**
     * Verify email with verification code
     * 
     * @param email User email
     * @param code  Verification code
     * @return true if verified successfully
     */
    boolean verifyEmail(String email, String code);

    /**
     * Resend email verification code
     * 
     * @param email User email
     */
    void resendEmailVerification(String email);

    /**
     * Generate and send password reset code
     * 
     * @param email User email
     */
    void sendPasswordResetCode(String email);

    /**
     * Reset password with reset code
     * 
     * @param email       User email
     * @param code        Reset code
     * @param newPassword New password
     * @return true if reset successfully
     */
    boolean resetPassword(String email, String code, String newPassword);
}
