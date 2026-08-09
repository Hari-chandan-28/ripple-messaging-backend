package com.backend.ripple.auth.controller

import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.auth.service.AuthService
import com.backend.ripple.dto.auth.AuthResponse
import com.backend.ripple.dto.auth.LoginRequset
import com.backend.ripple.dto.auth.SignupRequest
import com.backend.ripple.websocket.SessionStore
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController (private val authService: AuthService, private val userRepository: UserRepository) {
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignupRequest): ResponseEntity<Any> {
        val token = authService.signup(
            request.username,
            request.email,
            request.password);
        return ResponseEntity.ok(AuthResponse(token))
    }
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequset): ResponseEntity<Any>
    {
        val token = authService.login(
            request.email,
            request.password
        )
        return ResponseEntity.ok(AuthResponse(token))
    }
    @PatchMapping("/me/online-status")
    fun toggleOnlineStatus(@RequestBody request: OnlineStatusRequest): ResponseEntity<Void> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        user.showOnlineStatus = request.showOnlineStatus
        userRepository.save(user)
        return ResponseEntity.noContent().build()
    }

    data class OnlineStatusRequest(val showOnlineStatus: Boolean)
    @GetMapping("/online-status")
    fun getOnlineStatus(@RequestParam userIds: List<Long>): ResponseEntity<Map<Long, Any>> {
        val result = userIds.associate { id ->
            val user = userRepository.findById(id).orElse(null)
            val isOnline = if (user?.showOnlineStatus == true) {
                SessionStore.sessions[id]?.isOpen == true
            } else false
            val lastSeen = if (user?.showOnlineStatus == true) user.lastSeen?.toString() else null
            id to mapOf("isOnline" to isOnline, "lastSeen" to lastSeen)
        }
        return ResponseEntity.ok(result)
    }
}



