package com.example.Backend.repository;

import com.example.Backend.entity.ReorderSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reorder Setting Repository
 */
@Repository
public interface ReorderSettingRepository extends JpaRepository<ReorderSetting, UUID> {

        /**
         * Find reorder setting by product
         */
        Optional<ReorderSetting> findByProductId(UUID productId);

        /**
         * Find all enabled reorder settings
         */
        List<ReorderSetting> findByIsEnabledTrue();

        /**
         * Find products needing reorder
         */
        @Query("SELECT rs FROM ReorderSetting rs " +
                        "WHERE rs.isEnabled = true " +
                        "AND rs.product.stock <= rs.reorderPoint")
        List<ReorderSetting> findProductsNeedingReorder();

        /**
         * Find products below minimum stock
         */
        @Query("SELECT rs FROM ReorderSetting rs " +
                        "WHERE rs.isEnabled = true " +
                        "AND rs.minimumStock IS NOT NULL " +
                        "AND rs.product.stock < rs.minimumStock")
        List<ReorderSetting> findProductsBelowMinimum();

        /**
         * Find products with auto-reorder enabled
         */
        List<ReorderSetting> findByIsEnabledTrueAndAutoReorderTrue();

        /**
         * Find reorder settings by preferred supplier
         */
        List<ReorderSetting> findByPreferredSupplierId(UUID supplierId);

        /**
         * Check if product has reorder setting
         */
        boolean existsByProductId(UUID productId);

        /**
         * Count enabled reorder settings
         */
        Long countByIsEnabledTrue();
}
