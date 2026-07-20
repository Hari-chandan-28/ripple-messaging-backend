package com.backend.ripple.dto.profile

import com.backend.ripple.model.auth.User

data class ProfileIsPrivate(
    val userName: String,
    val isPrivate: Boolean
)
data class PrivacyRequest(val isPrivate: Boolean)