package com.backend.ripple.dto.group

data class GroupRequest(
    val name: String,
    val description: String,
)
data class GroupUpdateRequest(
    val name: String,
    val description: String,
    val groupId: Long,
)