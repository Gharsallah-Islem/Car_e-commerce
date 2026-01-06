package com.example.Backend.security;

public class SecurityConstants {
    // Constants like secret key

    public static final String JWT_SECRET = "your-secret-key";
    public static final long JWT_EXPIRATION = 3600000L; // 1 hour
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}
