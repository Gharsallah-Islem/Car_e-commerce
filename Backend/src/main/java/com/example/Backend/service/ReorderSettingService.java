package com.example.Backend.service;

import com.example.Backend.dto.ReorderSettingDTO;
import com.example.Backend.entity.ReorderSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReorderSettingService {
    ReorderSetting createReorderSetting(ReorderSettingDTO dto);
    ReorderSetting updateReorderSetting(UUID id, ReorderSettingDTO dto);
    ReorderSetting getById(UUID id);
    Optional<ReorderSetting> getByProductId(UUID productId);
    Page<ReorderSetting> getAllReorderSettings(Pageable pageable);
    List<ReorderSetting> getProductsBelowReorderPoint();
    void deleteReorderSetting(UUID id);
    void checkAndTriggerAutoReorders();
}
