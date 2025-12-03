package com.integmobile.backend.repository

import com.integmobile.backend.model.entity.OtpVerification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OtpVerificationRepository : JpaRepository<OtpVerification, String> {
    fun findByEmailAndOtpAndPurposeAndExpiresAtAfterAndVerifiedFalse(
        email: String,
        otp: String,
        purpose: String,
        currentTime: LocalDateTime
    ): OtpVerification?
    
    fun deleteByEmailAndPurpose(email: String, purpose: String)
}
