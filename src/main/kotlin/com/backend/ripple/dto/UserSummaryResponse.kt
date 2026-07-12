package com.backend.ripple.dto

data class UserSummaryResponse(
    val userId: Long,
    val username: String,
    val name: String? = null,
    val profilePic: String? = null
)