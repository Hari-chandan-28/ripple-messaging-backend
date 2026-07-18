package com.backend.ripple.auth.service

import com.backend.ripple.AlreadyExistsException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.UnauthorizedException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.auth.utils.JwtUtil
import com.backend.ripple.model.auth.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun signup (username :String, email :String, password :String) : String {
            if(userRepository.existsByEmail(email))
            {
                return "Email already registered!";
            }
            if (userRepository.existsByUsername(username)) {
            throw AlreadyExistsException("Username already taken")
            }
            val user = User(
                username = username,
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