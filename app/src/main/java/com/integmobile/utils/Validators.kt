package com.integmobile.utils

import android.util.Patterns

/**
 * Validation utilities for user input
 */
object Validators {

    /**
     * Validates email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password strength
     * Requirements: minimum 8 characters, at least one uppercase, one lowercase, one digit
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < Constants.MIN_PASSWORD_LENGTH) return false
        
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUpperCase && hasLowerCase && hasDigit
    }

    /**
     * Gets password strength message
     */
    fun getPasswordStrengthMessage(password: String): String {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < Constants.MIN_PASSWORD_LENGTH -> 
                "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters"
            !password.any { it.isUpperCase() } -> 
                "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> 
                "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> 
                "Password must contain at least one digit"
            else -> "Password is strong"
        }
    }

    /**
     * Validates phone number format
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val phonePattern = "^[+]?[0-9]{10,13}$"
        return phone.matches(phonePattern.toRegex())
    }

    /**
     * Validates OTP code
     */
    fun isValidOTP(otp: String): Boolean {
        return otp.length == Constants.OTP_LENGTH && otp.all { it.isDigit() }
    }

    /**
     * Validates that two passwords match
     */
    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && password.isNotEmpty()
    }

    /**
     * Validates name (non-empty and contains only letters and spaces)
     */
    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && name.matches("^[a-zA-Z\\s]+$".toRegex())
    }
}
