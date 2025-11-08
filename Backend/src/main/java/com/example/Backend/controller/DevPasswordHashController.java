package com.example.Backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TEMPORARY CONTROLLER - DELETE AFTER USE
 * This controller generates BCrypt password hashes for admin user setup
 * DELETE THIS FILE after setting up admin user for security reasons
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevPasswordHashController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/hash/{password}")
    public ResponseEntity<Map<String, String>> generateHash(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("sql", "UPDATE users SET password = '" + hash + "' WHERE username = 'admin';");
        
        return ResponseEntity.ok(response);
    }
}
