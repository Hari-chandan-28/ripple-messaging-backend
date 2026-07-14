package com.backend.ripple.dto.profile

import com.backend.ripple.dto.profile.RelationshipStatus
import jakarta.validation.constraints.NotBlank

data class ProfileCreationRequest (
    @field:NotBlank(message = "name is required")
    val name : String,
    val bio : String?= null,
    val profilePic : String? = null,
    val relationshipStatus : RelationshipStatus?=null,
)