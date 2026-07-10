package com.backend.ripple.profile

import com.backend.ripple.AlreadyExistsException
import com.backend.ripple.PrivateUserException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.ProfileResponse
import com.backend.ripple.dto.ProfileUpdateRequest
import com.backend.ripple.dto.RelationshipStatus
import com.backend.ripple.model.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ProfileService(val profileRepository: ProfileRepository, private val userRepository: UserRepository) {
    fun createProfile(name: String, bio: String?, profilePic: String?, relationshipStatus: RelationshipStatus?): Profile {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        if (profileRepository.findByUserId(userId).isPresent) {
            throw AlreadyExistsException("Profile already exists")
        }
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User $userId not found") }
        val profile = Profile(
            user = user,
            name = name,
            bio = bio,
            profilePic = profilePic,
            relationshipStatus = relationshipStatus
        )
        profileRepository.save(profile)
        return profile
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
        return ProfileResponse(
            name = profile.name,
            bio = profile.bio,
            profilePic = profile.profilePic,
            profile.relationshipStatus
        )
    }

    fun updateProfile(profile: ProfileUpdateRequest) {
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
    }
    fun deleteAccount() {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        userRepository.deleteById(userId)
    }
}