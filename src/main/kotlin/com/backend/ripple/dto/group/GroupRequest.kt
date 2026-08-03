package com.backend.ripple.dto.group

data class GroupRequest(
    val name: String,
    val description: String,
)
data class GroupUpdateRequest(
    val groupId: Long,
    val name: String?,
    val description: String?,
    val profilePic: String?,
)