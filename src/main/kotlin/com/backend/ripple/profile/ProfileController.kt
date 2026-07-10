package com.backend.ripple.profile

import com.backend.ripple.dto.ProfileCreationRequest
import com.backend.ripple.model.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController (val profileService: ProfileService) {
    @PostMapping("/create")
    fun create(@RequestBody profile: ProfileCreationRequest): ResponseEntity<Profile>
    {
        val newProfile= profileService.createProfile(
            profile.name,
            profile.bio,
            profile.profilePic,
            profile.relationshipStatus)
        return ResponseEntity.ok(newProfile)
    }
    @GetMapping("/get")
    fun get(): ResponseEntity<Profile>

}