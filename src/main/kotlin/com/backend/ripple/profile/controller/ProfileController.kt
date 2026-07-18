package com.backend.ripple.profile.controller

import com.backend.ripple.dto.profile.ProfileCreationRequest
import com.backend.ripple.dto.profile.ProfileResponse
import com.backend.ripple.dto.profile.ProfileUpdateRequest
import com.backend.ripple.profile.service.ProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController (val profileService: ProfileService) {
    @PostMapping("/create")
    fun create(@RequestBody profile: ProfileCreationRequest): ResponseEntity<ProfileResponse>
    {
        val newProfile= profileService.createProfile(profile)
        return ResponseEntity.ok(newProfile)
    }
    @GetMapping("/{userId}")
    fun getProfile(@PathVariable userId:Long): ResponseEntity<ProfileResponse>
    {
        val profile = profileService.getProfile(userId)
        return ResponseEntity.ok(profile)
    }
    @PutMapping("/update")
    fun updateProfile(@RequestBody profile: ProfileUpdateRequest): ResponseEntity<ProfileResponse>
    {
        val updated = profileService.updateProfile(profile)
        return ResponseEntity.ok(updated)
    }
    @DeleteMapping("/remove")
    fun removeProfile(): ResponseEntity<Void>
    {
        profileService.deleteAccount();
        return ResponseEntity.noContent().build()
    }

}