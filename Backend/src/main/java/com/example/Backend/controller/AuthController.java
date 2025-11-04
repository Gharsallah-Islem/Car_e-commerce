package com.example.Backend.controller;

import com.example.Backend.dto.UserDTO;
import com.example.Backend.entity.User;
import com.example.Backend.security.JwtTokenProvider;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Support both username and email for login
        String loginIdentifier = loginRequest.getEmail() != null ? loginRequest.getEmail() : loginRequest.getUsername();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginIdentifier,
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Get user details
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.getUserById(userPrincipal.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("tokenType", "Bearer");

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("firstName", extractFirstName(user.getFullName()));
        userData.put("lastName", extractLastName(user.getFullName()));
        userData.put("fullName", user.getFullName());
        userData.put("phoneNumber", user.getPhone());
        userData.put("address", user.getAddress());
        userData.put("role", user.getRole().getName());

        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        // Create the user
        User newUser = userService.createUser(userDTO);

        // Auto-login the user after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        newUser.getUsername(),
                        userDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Prepare response with token and user data
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("tokenType", "Bearer");

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", newUser.getId());
        userData.put("username", newUser.getUsername());
        userData.put("email", newUser.getEmail());
        userData.put("firstName", extractFirstName(newUser.getFullName()));
        userData.put("lastName", extractLastName(newUser.getFullName()));
        userData.put("fullName", newUser.getFullName());
        userData.put("phoneNumber", newUser.getPhone());
        userData.put("address", newUser.getAddress());
        userData.put("role", newUser.getRole().getName());

        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    private String extractFirstName(String fullName) {
        if (fullName == null)
            return "";
        String[] parts = fullName.split(" ", 2);
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null)
            return "";
        String[] parts = fullName.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstName", extractFirstName(user.getFullName()));
        response.put("lastName", extractLastName(user.getFullName()));
        response.put("fullName", user.getFullName());
        response.put("name", user.getFullName());
        response.put("phoneNumber", user.getPhone());
        response.put("address", user.getAddress());
        response.put("role", user.getRole().getName());

        return ResponseEntity.ok(response);
    }

    @lombok.Data
    public static class LoginRequest {
        private String username; // Can be username or email
        private String email; // Alternative field for email
        private String password;
    }
}
