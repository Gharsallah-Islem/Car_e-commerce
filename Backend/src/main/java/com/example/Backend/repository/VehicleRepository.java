package com.example.Backend.repository;

import com.example.Backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    /**
     * Find all vehicles for a specific user
     */
    List<Vehicle> findByUserId(UUID userId);

    /**
     * Find vehicles by brand
     */
    List<Vehicle> findByBrand(String brand);

    /**
     * Find vehicles by brand and model
     */
    List<Vehicle> findByBrandAndModel(String brand, String model);

    /**
     * Find vehicles by brand, model, and year
     */
    List<Vehicle> findByBrandAndModelAndYear(String brand, String model, Integer year);

    /**
     * Find vehicles by year range
     */
    List<Vehicle> findByYearBetween(Integer startYear, Integer endYear);

    /**
     * Search vehicles by brand or model
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
            "LOWER(v.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Vehicle> searchVehicles(@Param("searchTerm") String searchTerm);

    /**
     * Get distinct brands
     */
    @Query("SELECT DISTINCT v.brand FROM Vehicle v ORDER BY v.brand")
    List<String> findDistinctBrands();

    /**
     * Get distinct models for a brand
     */
    @Query("SELECT DISTINCT v.model FROM Vehicle v WHERE v.brand = :brand ORDER BY v.model")
    List<String> findDistinctModelsByBrand(@Param("brand") String brand);

    /**
     * Get distinct years for a brand and model
     */
    @Query("SELECT DISTINCT v.year FROM Vehicle v WHERE v.brand = :brand AND v.model = :model ORDER BY v.year DESC")
    List<Integer> findDistinctYearsByBrandAndModel(@Param("brand") String brand, @Param("model") String model);
}
