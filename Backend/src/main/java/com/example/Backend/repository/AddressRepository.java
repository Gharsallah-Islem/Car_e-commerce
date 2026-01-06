package com.example.Backend.repository;

import com.example.Backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(UUID userId);

    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);
}
