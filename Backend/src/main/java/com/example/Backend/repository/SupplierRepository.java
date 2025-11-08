package com.example.Backend.repository;

import com.example.Backend.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Supplier Repository
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Find supplier by name
     */
    Optional<Supplier> findByName(String name);

    /**
     * Find active suppliers
     */
    List<Supplier> findByIsActiveTrue();

    /**
     * Find suppliers by country
     */
    List<Supplier> findByCountry(String country);

    /**
     * Search suppliers by name or company name
     */
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm);

    /**
     * Find suppliers with rating above threshold
     */
    @Query("SELECT s FROM Supplier s WHERE s.rating >= :minRating AND s.isActive = true")
    List<Supplier> findByRatingGreaterThanEqual(@Param("minRating") Double minRating);

    /**
     * Count active suppliers
     */
    Long countByIsActiveTrue();

    /**
     * Find suppliers providing a specific product
     */
    @Query("SELECT s FROM Supplier s JOIN s.products p WHERE p.id = :productId")
    List<Supplier> findSuppliersByProductId(@Param("productId") UUID productId);
}
