package com.example.Backend.service.impl;

import com.example.Backend.dto.AdminDTO;
import com.example.Backend.entity.Admin;
import com.example.Backend.repository.AdminRepository;
import com.example.Backend.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Admin createAdmin(AdminDTO adminDTO) {
        // Check if username already exists
        if (adminRepository.existsByUsername(adminDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(adminDTO.getUsername());
        admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        admin.setEmail(adminDTO.getEmail());
        admin.setFullName(adminDTO.getFullName());

        return adminRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public Admin getAdminById(UUID adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));
    }

    @Override
    @Transactional(readOnly = true)
    public Admin getAdminByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    @Transactional
    public Admin updateAdmin(UUID adminId, AdminDTO adminDTO) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));

        if (adminDTO.getUsername() != null && !adminDTO.getUsername().equals(admin.getUsername())) {
            if (adminRepository.existsByUsername(adminDTO.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            admin.setUsername(adminDTO.getUsername());
        }

        if (adminDTO.getEmail() != null && !adminDTO.getEmail().equals(admin.getEmail())) {
            if (adminRepository.existsByEmail(adminDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            admin.setEmail(adminDTO.getEmail());
        }

        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        }

        if (adminDTO.getFullName() != null) {
            admin.setFullName(adminDTO.getFullName());
        }

        return adminRepository.save(admin);
    }

    @Override
    @Transactional
    public void deleteAdmin(UUID adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));
        adminRepository.delete(admin);
    }

    @Override
    @Transactional
    public Admin activateAdmin(UUID adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));
        admin.activate();
        return adminRepository.save(admin);
    }

    @Override
    @Transactional
    public Admin deactivateAdmin(UUID adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with id: " + adminId));
        admin.deactivate();
        return adminRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> getActiveAdmins() {
        return adminRepository.findByIsActive(true);
    }
}
