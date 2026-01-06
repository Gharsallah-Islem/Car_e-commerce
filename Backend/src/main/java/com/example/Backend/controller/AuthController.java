package com.example.Backend.controller;

import com.example.Backend.dto.*;
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

        // Get user details to check email verification
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.getUserById(userPrincipal.getId());

        // Check if email is verified (except for admin/super-admin)
        if (!user.isAdmin() && !user.isSuperAdmin() && !user.getIsEmailVerified()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("emailVerified", false);
            response.put("message",
                    "Please verify your email before logging in. Check your inbox for the verification code.");
            return ResponseEntity.status(403).body(response);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

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
        userData.put("profilePicture", user.getProfilePicture());
        userData.put("role", user.getRole().getName());

        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        // Create the user
        User newUser = userService.createUser(userDTO);

        // Send email verification
        try {
            userService.sendEmailVerification(newUser.getEmail());
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message",
                "Registration successful! Please check your email to verify your account before logging in.");

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", newUser.getId());
        userData.put("username", newUser.getUsername());
        userData.put("email", newUser.getEmail());
        userData.put("fullName", newUser.getFullName());

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
        response.put("profilePicture", user.getProfilePicture());
        response.put("role", user.getRole().getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            boolean verified = userService.verifyEmail(request.getEmail(), request.getCode());

            Map<String, Object> response = new HashMap<>();
            response.put("success", verified);
            response.put("message", "Email verified successfully! You can now login.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            userService.resendEmailVerification(request.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Verification code resent successfully. Please check your email.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.sendPasswordResetCode(request.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset code sent to your email. Please check your inbox.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            boolean reset = userService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", reset);
            response.put("message", "Password reset successfully! You can now login with your new password.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @lombok.Data
    public static class LoginRequest {
        private String username; // Can be username or email
        private String email; // Alternative field for email
        private String password;
    }
}
