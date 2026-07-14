package com.backend.ripple.dto.profile

import com.backend.ripple.dto.profile.RelationshipStatus

data class ProfileUpdateRequest(
    val name: String,
    val bio: String?,
    val profilePic: String?,
    val relationshipStatus: RelationshipStatus?,
    val isPrivate: Boolean
)