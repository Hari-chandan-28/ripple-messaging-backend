package com.backend.ripple.auth.controller

import com.backend.ripple.auth.service.AuthService
import com.backend.ripple.dto.auth.AuthResponse
import com.backend.ripple.dto.auth.LoginRequset
import com.backend.ripple.dto.auth.SignupRequest
import com.backend.ripple.websocket.SessionStore
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController (private val authService: AuthService) {
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
    @GetMapping("/online-status")
    fun getOnlineStatus(@RequestParam userIds: List<Long>): ResponseEntity<Map<Long, Boolean>> {
        val result = userIds.associateWith { SessionStore.sessions[it]?.isOpen == true }
        return ResponseEntity.ok(result)
    }
}



