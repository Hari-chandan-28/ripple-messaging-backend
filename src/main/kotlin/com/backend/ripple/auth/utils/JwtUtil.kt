package com.backend.ripple.auth.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtUtil
{
    @Value("\${jwt.secret}")
    private lateinit var secret : String
    @Value("\${jwt.expiration}")
    var expiryHours : Long = 0

    private val key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }
    fun generateToken(email: String ,userId:Long ): String{
        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiryHours ))
            .signWith(key)
            .compact()
    }
    fun isTokenValid(token: String): Boolean{
        return try{
            getClaims(token)
            true
        } catch (e : Exception)
        {
            false
        }
    }
    fun extractUserId(token: String): Long {
        return getClaims(token)["userId"] as Long
    }
    fun extractEmail(token: String): String {
        return getClaims(token).subject
    }
    private fun getClaims(token: String) = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload

}