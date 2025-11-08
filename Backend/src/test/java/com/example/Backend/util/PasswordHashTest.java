package com.example.Backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Test class to verify BCrypt password hashing
 * Run this to generate correct password hashes
 */
public class PasswordHashTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // Test passwords
        String password1 = "admin123";
        String password2 = "super123";

        // Generate hashes
        String hash1 = encoder.encode(password1);
        String hash2 = encoder.encode(password2);

        System.out.println("=".repeat(60));
        System.out.println("PASSWORD HASH GENERATOR");
        System.out.println("=".repeat(60));

        System.out.println("\n1. Password: " + password1);
        System.out.println("   Hash: " + hash1);
        System.out.println("   Verify: " + encoder.matches(password1, hash1));

        System.out.println("\n2. Password: " + password2);
        System.out.println("   Hash: " + hash2);
        System.out.println("   Verify: " + encoder.matches(password2, hash2));

        // Test the hash from the SQL script
        String sqlHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu";
        System.out.println("\n3. Testing SQL script hash:");
        System.out.println("   Hash: " + sqlHash);
        System.out.println("   Matches 'admin123': " + encoder.matches(password1, sqlHash));
        System.out.println("   Matches 'Admin123': " + encoder.matches("Admin123", sqlHash));
        System.out.println("   Matches 'ADMIN123': " + encoder.matches("ADMIN123", sqlHash));

        System.out.println("\n" + "=".repeat(60));
        System.out.println("SQL INSERT STATEMENT:");
        System.out.println("=".repeat(60));
        System.out.println("\nINSERT INTO users (");
        System.out.println("    id, username, email, password, full_name,");
        System.out.println("    address, phone, role_id, created_at, updated_at");
        System.out.println(") VALUES (");
        System.out.println("    gen_random_uuid(), 'admin', 'admin@carparts.com',");
        System.out.println("    '" + hash1 + "',");
        System.out.println("    'System Administrator', 'Admin Office', '+1234567890',");
        System.out.println("    3, NOW(), NOW()");
        System.out.println(");");
        System.out.println();
    }
}
