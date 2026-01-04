package com.example.Backend.service.impl;

import com.example.Backend.dto.UserDTO;
import com.example.Backend.entity.Role;
import com.example.Backend.entity.User;
import com.example.Backend.repository.RoleRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.EmailService;
import com.example.Backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public User createUser(UserDTO userDTO) {
        // Auto-generate username from email if not provided
        String username = userDTO.getUsername();
        if (username == null || username.trim().isEmpty()) {
            // Extract username from email (part before @)
            username = userDTO.getEmail().split("@")[0];
            // Remove any special characters and ensure it's at least 3 characters
            username = username.replaceAll("[^a-zA-Z0-9]", "");
            if (username.length() < 3) {
                username = username + "user";
            }
            // Make sure it's unique by adding numbers if needed
            String baseUsername = username;
            int counter = 1;
            while (usernameExists(username)) {
                username = baseUsername + counter;
                counter++;
            }
        } else if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (emailExists(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());

        // Set default role to CLIENT
        Role role = roleRepository.findByName("CLIENT")
                .orElse(roleRepository.findById(1) // Role.CLIENT = 1
                        .orElseThrow(() -> new EntityNotFoundException("Role not found")));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Override
    public User updateUser(UUID id, UserDTO userDTO) {
        User user = getUserById(id);

        // Check if username is being changed and if it already exists
        if (userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
            if (!user.getUsername().equals(userDTO.getUsername()) && usernameExists(userDTO.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(userDTO.getUsername());
        }

        // Check if email is being changed and if it already exists
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            if (!user.getEmail().equals(userDTO.getEmail()) && emailExists(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }

        // Update optional fields only if provided
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }

        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }

        if (userDTO.getPhoneNumber() != null) {
            user.setPhone(userDTO.getPhoneNumber());
        }

        if (userDTO.getProfilePicture() != null) {
            user.setProfilePicture(userDTO.getProfilePicture());
        }

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsers(LocalDateTime since) {
        return userRepository.findActiveUsers(since);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUsersByRole(String roleName) {
        return userRepository.countByRoleName(roleName);
    }

    @Override
    public String sendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        // Generate 6-digit verification code
        String verificationCode = generateVerificationCode();

        // Set verification token and expiry (15 minutes from now)
        user.setEmailVerificationToken(verificationCode);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationCode);

        return verificationCode; // Return for testing purposes
    }

    @Override
    public boolean verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        // Check if code matches and is not expired
        if (user.getEmailVerificationToken() == null) {
            throw new IllegalStateException("No verification code found. Please request a new one.");
        }

        if (!user.getEmailVerificationToken().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification code has expired. Please request a new one.");
        }

        // Mark email as verified and clear the token
        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        return true;
    }

    @Override
    public void resendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        if (user.getIsEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Generate new verification code
        sendEmailVerification(email);
    }

    @Override
    public void sendPasswordResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        // Generate 6-digit reset code
        String resetCode = generateVerificationCode();

        // Set reset token and expiry (15 minutes from now)
        user.setPasswordResetToken(resetCode);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetCode);
    }

    @Override
    public boolean resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        // Check if code matches and is not expired
        if (user.getPasswordResetToken() == null) {
            throw new IllegalStateException("No reset code found. Please request a new one.");
        }

        if (!user.getPasswordResetToken().equals(code)) {
            throw new IllegalArgumentException("Invalid reset code");
        }

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Reset code has expired. Please request a new one.");
        }

        // Update password and clear the token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return true;
    }

    @Override
    public User updateUserRole(UUID id, String roleName) {
        User user = getUserById(id);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Generate a random 6-digit verification code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generates 6-digit number
        return String.valueOf(code);
    }
}
