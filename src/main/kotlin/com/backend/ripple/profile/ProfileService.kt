package com.backend.ripple.profile

import com.backend.ripple.AlreadyExistsException
import com.backend.ripple.PrivateUserException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.profile.ProfileCreationRequest
import com.backend.ripple.dto.profile.ProfileResponse
import com.backend.ripple.dto.profile.ProfileUpdateRequest
import com.backend.ripple.dto.profile.RelationshipStatus
import com.backend.ripple.model.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class ProfileService(val profileRepository: ProfileRepository, private val userRepository: UserRepository) {

    fun createProfile(profile : ProfileCreationRequest): ProfileResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        if (profileRepository.findByUserId(userId).isPresent) {
            throw AlreadyExistsException("Profile already exists")
        }
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User $userId not found") }
        val profile = Profile(
            user = user,
            name = profile.name,
            bio = profile.bio,
            profilePic = profile.profilePic,
            relationshipStatus = profile.relationshipStatus,
        )
        val newProfile = profileRepository.save(profile)
        return toResponse(newProfile)
    }

    fun getProfile(userId: Long): ProfileResponse {
        val curId = SecurityContextHolder.getContext().authentication?.principal as Long
        val profile =
            profileRepository.findByUserId(userId).orElseThrow { ResourceNotFoundException("Profile not found") }
        if (curId != userId) {
            // TODO: allow friends to view private profiles once friendship module is built
            val isPrivate = profile.user.isPrivate
            if (isPrivate) {
                throw PrivateUserException("Can't access this resource")
            }
        }
        return toResponse(profile)
    }
    @Transactional
    fun updateProfile(profile: ProfileUpdateRequest): ProfileResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val userProfile =
            profileRepository.findByUserId(userId).orElseThrow { ResourceNotFoundException("Profile not found") }
        userProfile.name = profile.name
        userProfile.profilePic = profile.profilePic
        userProfile.bio = profile.bio
        userProfile.relationshipStatus = profile.relationshipStatus
        userProfile.user.isPrivate = profile.isPrivate
        profileRepository.save(userProfile)
        userRepository.save(userProfile.user)
        return toResponse(userProfile)
    }
    fun deleteAccount() {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        userRepository.deleteById(userId)
    }
    private fun toResponse(profile: Profile): ProfileResponse {
        return ProfileResponse(
            name = profile.name,
            bio = profile.bio,
            profilePic = profile.profilePic,
            relationshipStatus = profile.relationshipStatus,
            isPrivate = profile.user.isPrivate
        )
    }
}