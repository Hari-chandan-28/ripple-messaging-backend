package com.backend.ripple.dto.profile


data class ProfileResponse(
    val name: String,
    val bio: String?,
    val profilePic: String?,
    val relationshipStatus: RelationshipStatus?,
    val isPrivate: Boolean,
    val friendshipStatus: Int?,
    val isSender: Boolean?
)