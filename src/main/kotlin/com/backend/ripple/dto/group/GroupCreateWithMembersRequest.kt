package com.backend.ripple.dto.group

data class GroupCreateWithMembersRequest(
    val name: String,
    val description: String? = null,
    val memberIds: List<Long> = emptyList(),
)