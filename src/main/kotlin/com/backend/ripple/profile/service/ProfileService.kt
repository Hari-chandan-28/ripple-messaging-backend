package com.backend.ripple.profile.service

import com.backend.ripple.AlreadyExistsException
import com.backend.ripple.PrivateUserException
import com.backend.ripple.ResourceNotFoundException
import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.profile.PrivacyRequest
import com.backend.ripple.dto.profile.ProfileCreationRequest
import com.backend.ripple.dto.profile.ProfileIsPrivate
import com.backend.ripple.dto.profile.ProfileResponse
import com.backend.ripple.dto.profile.ProfileUpdateRequest
import com.backend.ripple.friendship.repository.FriendshipRepository
import com.backend.ripple.model.profile.Profile
import com.backend.ripple.profile.repository.ProfileRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService(
    val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository
) {

    fun createProfile(profile : ProfileCreationRequest): ProfileResponse {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        if (profileRepository.findByUserId(userId).isPresent) {
            throw AlreadyExistsException("Profile already exists")
        }
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User $userId not found") }
        val profile1 = Profile(
            user = user,
            name = profile.name,
            bio = profile.bio,
            profilePic = profile.profilePic,
            relationshipStatus = profile.relationshipStatus,
        )
        val newProfile = profileRepository.save(profile1)
        user.isPrivate = profile.isPrivate
        userRepository.save(user)
        return toResponse(newProfile,null,null)
    }

    fun getProfile(userId: Long): ProfileResponse {
        val curId = SecurityContextHolder.getContext().authentication?.principal as Long
        val profile = profileRepository.findByUserId(userId).orElseThrow { ResourceNotFoundException("Profile not found") }
        var friendshipStatus: Int? = null
        var isSender: Boolean? = null
        if (curId != userId) {
            val isPrivate = profile.user.isPrivate
            if (isPrivate) {
                val areFriends = friendshipRepository.findBySender_UserIdAndReceiver_UserId(curId, userId).isPresent ||
                        friendshipRepository.findBySender_UserIdAndReceiver_UserId(userId, curId).isPresent
                if (!areFriends) {
                    throw PrivateUserException("Can't access this resource")
                }
            }
            val sent = friendshipRepository.findBySender_UserIdAndReceiver_UserId(curId, userId)
            val received = friendshipRepository.findBySender_UserIdAndReceiver_UserId(userId, curId)
            when {
                sent.isPresent -> {
                    friendshipStatus = sent.get().status
                    isSender = true
                }
                received.isPresent -> {
                    friendshipStatus = received.get().status
                    isSender = false
                }
            }
        }
        return toResponse(profile,friendshipStatus, isSender)
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
        return toResponse(userProfile,null,null)
    }
    fun updateIsPrivate(privacy : PrivacyRequest): ProfileIsPrivate {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        val userProfile = userRepository.findByUserId(userId).orElseThrow { ResourceNotFoundException("User not found") }
        userProfile.isPrivate = privacy.isPrivate
        userRepository.save(userProfile)
        return ProfileIsPrivate(
            userProfile.username,
            userProfile.isPrivate,
        )
    }
    fun deleteAccount() {
        val userId = SecurityContextHolder.getContext().authentication?.principal as Long
        userRepository.deleteById(userId)
    }
    private fun toResponse(profile: Profile, friendshipStatus: Int?, isSender: Boolean?): ProfileResponse {
        return ProfileResponse(
            name = profile.name,
            bio = profile.bio,
            profilePic = profile.profilePic,
            relationshipStatus = profile.relationshipStatus,
            isPrivate = profile.user.isPrivate,
            friendshipStatus = friendshipStatus,
            isSender = isSender
        )
    }
}