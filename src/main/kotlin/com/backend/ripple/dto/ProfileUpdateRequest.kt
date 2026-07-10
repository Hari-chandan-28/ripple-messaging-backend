package com.backend.ripple.dto

data class ProfileUpdateRequest(
val name: String,
val bio: String?,
val profilePic: String?,
val relationshipStatus: RelationshipStatus?,
val isPrivate: Boolean
)