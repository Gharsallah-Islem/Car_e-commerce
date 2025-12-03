package com.integmobile.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {
    
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${jwt.expiration}")
    private var jwtExpiration: Long = 0
    
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
    
    fun generateToken(userId: String, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)
        
        return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    fun getUserIdFromToken(token: String): String {
        val claims: Claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
        
        return claims.subject
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}
