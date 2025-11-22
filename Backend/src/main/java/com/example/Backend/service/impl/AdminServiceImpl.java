package com.example.Backend.service.impl;

import com.example.Backend.dto.AdminDTO;
import com.example.Backend.entity.Role;
import com.example.Backend.entity.User;
import com.example.Backend.repository.RoleRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createAdmin(AdminDTO adminDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(adminDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(adminDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("Role ADMIN not found"));

        User user = new User();
        user.setUsername(adminDTO.getUsername());
        user.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        user.setEmail(adminDTO.getEmail());
        user.setFullName(adminDTO.getFullName());
        user.setRole(adminRole);
        user.setIsActive(true);
        user.setIsEmailVerified(true); // Admins are verified by default

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getAdminById(UUID adminId) {
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));

        if (!user.isAdmin() && !user.isSuperAdmin()) {
            throw new EntityNotFoundException("User is not an admin");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getAdminByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with username: " + username));

        if (!user.isAdmin() && !user.isSuperAdmin()) {
            throw new EntityNotFoundException("User is not an admin");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllAdmins() {
        return userRepository.findByRoleName(Role.ADMIN);
    }

    @Override
    @Transactional
    public User updateAdmin(UUID adminId, AdminDTO adminDTO) {
        User user = getAdminById(adminId);

        if (adminDTO.getUsername() != null && !adminDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(adminDTO.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(adminDTO.getUsername());
        }

        if (adminDTO.getEmail() != null && !adminDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(adminDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(adminDTO.getEmail());
        }

        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        }

        if (adminDTO.getFullName() != null) {
            user.setFullName(adminDTO.getFullName());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAdmin(UUID adminId) {
        User user = getAdminById(adminId);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setIsActive(true);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveAdmins() {
        return userRepository.findByRoleName(Role.ADMIN).stream()
                .filter(User::getIsActive)
                .collect(Collectors.toList());
    }
}
