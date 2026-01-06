package com.example.Backend.controller;

import com.example.Backend.entity.Address;
import com.example.Backend.entity.User;
import com.example.Backend.repository.AddressRepository;
import com.example.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Address>> getMyAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        String identifier = userDetails.getUsername();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<Address> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String identifier = userDetails.getUsername();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = new Address();
        address.setStreet(request.get("street"));
        address.setCity(request.get("city"));
        address.setPostalCode(request.get("postalCode"));
        address.setCountry(request.getOrDefault("country", "Tunisie"));
        address.setIsDefault(false);
        address.setUser(user);

        Address savedAddress = addressRepository.save(address);
        return ResponseEntity.ok(savedAddress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String identifier = userDetails.getUsername();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Ensure user owns this address
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        address.setStreet(request.get("street"));
        address.setCity(request.get("city"));
        address.setPostalCode(request.get("postalCode"));
        if (request.containsKey("country")) {
            address.setCountry(request.get("country"));
        }

        Address updatedAddress = addressRepository.save(address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String identifier = userDetails.getUsername();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Ensure user owns this address
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        addressRepository.delete(address);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<Address> setDefaultAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String identifier = userDetails.getUsername();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Ensure user owns this address
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Remove default from all other addresses
        List<Address> allAddresses = addressRepository.findByUserId(user.getId());
        for (Address a : allAddresses) {
            a.setIsDefault(false);
            addressRepository.save(a);
        }

        // Set this address as default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);
        return ResponseEntity.ok(updatedAddress);
    }
}
