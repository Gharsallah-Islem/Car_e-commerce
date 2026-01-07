package com.example.carpartsecom.util

import android.util.Patterns

/**
 * Utility object for common input validation functions
 */
object ValidationUtils {

    // Email validation
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun getEmailError(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    // Password validation
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6 &&
               password.contains(Regex("[A-Za-z]")) &&
               password.contains(Regex("[0-9]"))
    }

    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            !password.contains(Regex("[A-Za-z]")) -> "Password must contain at least one letter"
            !password.contains(Regex("[0-9]")) -> "Password must contain at least one number"
            else -> null
        }
    }

    // Name validation (first name, last name)
    fun isValidName(name: String): Boolean {
        return name.length >= 2 && name.matches(Regex("^[a-zA-Z\\s'-]+$"))
    }

    fun getNameError(name: String, fieldName: String = "Name"): String? {
        return when {
            name.isEmpty() -> "$fieldName is required"
            name.length < 2 -> "$fieldName must be at least 2 characters"
            !name.matches(Regex("^[a-zA-Z\\s'-]+$")) -> "$fieldName can only contain letters, spaces, hyphens, and apostrophes"
            else -> null
        }
    }

    // Phone validation
    fun isValidPhone(phone: String): Boolean {
        if (phone.isEmpty()) return true // Phone is often optional
        val digits = phone.replace(Regex("[^0-9]"), "")
        return digits.length in 8..15 && phone.matches(Regex("^[+]?[0-9\\s\\-()]+$"))
    }

    fun getPhoneError(phone: String, required: Boolean = false): String? {
        if (phone.isEmpty()) {
            return if (required) "Phone number is required" else null
        }
        val digits = phone.replace(Regex("[^0-9]"), "")
        return when {
            digits.length < 8 -> "Phone number must have at least 8 digits"
            digits.length > 15 -> "Phone number must have at most 15 digits"
            !phone.matches(Regex("^[+]?[0-9\\s\\-()]+$")) -> "Phone can only contain digits, +, -, (), and spaces"
            else -> null
        }
    }

    // OTP validation (6-digit code)
    fun isValidOtp(otp: String): Boolean {
        return otp.matches(Regex("^[0-9]{6}$"))
    }

    fun getOtpError(otp: String): String? {
        return when {
            otp.isEmpty() -> "OTP code is required"
            !otp.matches(Regex("^[0-9]+$")) -> "OTP code must contain only digits"
            otp.length != 6 -> "OTP code must be 6 digits"
            else -> null
        }
    }

    // Latitude validation
    fun isValidLatitude(lat: Double?): Boolean {
        return lat != null && lat >= -90 && lat <= 90
    }

    fun getLatitudeError(latStr: String): String? {
        if (latStr.isEmpty()) return "Latitude is required"
        val lat = latStr.toDoubleOrNull() ?: return "Invalid number"
        return when {
            lat < -90 -> "Latitude must be at least -90"
            lat > 90 -> "Latitude must be at most 90"
            else -> null
        }
    }

    // Longitude validation
    fun isValidLongitude(lng: Double?): Boolean {
        return lng != null && lng >= -180 && lng <= 180
    }

    fun getLongitudeError(lngStr: String): String? {
        if (lngStr.isEmpty()) return "Longitude is required"
        val lng = lngStr.toDoubleOrNull() ?: return "Invalid number"
        return when {
            lng < -180 -> "Longitude must be at least -180"
            lng > 180 -> "Longitude must be at most 180"
            else -> null
        }
    }

    // Address validation
    fun isValidAddress(address: String): Boolean {
        return address.length >= 5
    }

    fun getAddressError(address: String): String? {
        return when {
            address.isEmpty() -> "Address is required"
            address.length < 5 -> "Please enter a valid address"
            else -> null
        }
    }

    // Generic required field validation
    fun getRequiredFieldError(value: String, fieldName: String): String? {
        return if (value.isEmpty()) "$fieldName is required" else null
    }

    // Text length validation
    fun getTextLengthError(
        text: String,
        fieldName: String,
        minLength: Int = 0,
        maxLength: Int = Int.MAX_VALUE,
        required: Boolean = true
    ): String? {
        return when {
            text.isEmpty() && required -> "$fieldName is required"
            text.length < minLength -> "$fieldName must be at least $minLength characters"
            text.length > maxLength -> "$fieldName must be at most $maxLength characters"
            else -> null
        }
    }
}

