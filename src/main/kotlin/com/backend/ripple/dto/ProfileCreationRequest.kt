package com.backend.ripple.dto

import jakarta.validation.constraints.NotBlank

data class ProfileCreationRequest (
    @field:NotBlank(message = "name is required")
    val name : String,
    val bio : String?= null,
    val profilePic : String? = null,
    val relationshipStatus : RelationshipStatus?=null,
)