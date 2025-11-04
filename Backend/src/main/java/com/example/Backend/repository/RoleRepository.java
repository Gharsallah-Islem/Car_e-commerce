package com.example.Backend.repository;

import com.example.Backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find role by name
     * 
     * @param name Role name (CLIENT, SUPPORT, ADMIN, SUPER_ADMIN)
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role exists by name
     * 
     * @param name Role name
     * @return true if role exists
     */
    boolean existsByName(String name);
}
