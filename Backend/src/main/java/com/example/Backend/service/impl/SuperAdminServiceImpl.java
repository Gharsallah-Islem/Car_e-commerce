package com.example.Backend.service.impl;

import com.example.Backend.entity.SuperAdmin;
import com.example.Backend.repository.SuperAdminRepository;
import com.example.Backend.service.SuperAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {

    private final SuperAdminRepository superAdminRepository;

    @Override
    @Transactional(readOnly = true)
    public SuperAdmin getSuperAdminById(UUID superAdminId) {
        return superAdminRepository.findById(superAdminId)
                .orElseThrow(() -> new EntityNotFoundException("SuperAdmin not found with id: " + superAdminId));
    }

    @Override
    @Transactional(readOnly = true)
    public SuperAdmin getSuperAdminByUsername(String username) {
        return superAdminRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("SuperAdmin not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuperAdmin> getActiveSuperAdmins() {
        return superAdminRepository.findByIsActive(true);
    }
}
