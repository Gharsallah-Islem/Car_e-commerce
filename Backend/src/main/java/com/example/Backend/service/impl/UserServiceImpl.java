package com.example.Backend.service.impl;

import com.example.Backend.dto.UserDTO;
import com.example.Backend.entity.Role;
import com.example.Backend.entity.User;
import com.example.Backend.repository.RoleRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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
        if (!user.getUsername().equals(userDTO.getUsername()) && usernameExists(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(userDTO.getEmail()) && emailExists(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setAddress(userDTO.getAddress());

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
}
