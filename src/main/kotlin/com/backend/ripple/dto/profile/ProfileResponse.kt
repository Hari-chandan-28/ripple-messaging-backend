package com.backend.ripple.dto.profile


data class ProfileResponse(
    val userId: Long,
    val username: String,
    val name: String?,
    val bio: String?,
    val profilePic: String?,
    val relationshipStatus: String?,
    val isPrivate: Boolean,
    val friendshipStatus: Int?,
    val isSender: Boolean?,
    val lastSeen: String?,  // add this
)