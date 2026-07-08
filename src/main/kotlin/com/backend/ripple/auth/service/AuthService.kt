package com.backend.ripple.auth.service

import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.UnauthorizedException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.auth.utils.JwtUtil
import com.backend.ripple.model.User
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun signup (userName :String, email :String, password :String) : String {
            if(userRepository.existsByEmail(email))
            {
                return "Email already registered!";
            }
            val user = User(
                username = userName,
                email = email,
                password = passwordEncoder.encode(password)
            )
        userRepository.save(user);
        val token = jwtUtil.generateToken(user.email,user.userId);
        return token
    }
    fun login (email :String,password :String) : String{
        var user = userRepository.findByEmail(email)
            .orElseThrow{ ResourceNotFoundException("Email not found")}
        if(!passwordEncoder.matches(password,user.password))
        {
            throw UnauthorizedException("Invalid password")
        }
        return jwtUtil.generateToken(user.email,user.userId);
    }
}