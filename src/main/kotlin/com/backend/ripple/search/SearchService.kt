package com.backend.ripple.search

import com.backend.ripple.auth.repository.UserRepository
import com.backend.ripple.dto.friendship.UserSummaryResponse
import com.backend.ripple.model.User
import com.backend.ripple.profile.ProfileRepository
import org.springframework.stereotype.Service

@Service
class SearchService (private val userRepository: UserRepository, private val profileRepository: ProfileRepository) {
    fun search(username: String): List<UserSummaryResponse>{
        return userRepository.findByUsernameContainingIgnoreCaseAndIsPrivate(username, false).map { user ->
            val profile = profileRepository.findByUserId(user.userId).orElse(null)
            UserSummaryResponse(
                userId = user.userId,
                username = user.username,
                name = profile?.name,
                profilePic = profile?.profilePic
            )
        }
    }
}