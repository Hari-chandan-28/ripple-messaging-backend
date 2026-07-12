package com.backend.ripple.dto

import com.backend.ripple.group.GroupRole

data class GroupMemberResponse (
    val groupName: String,
    val username: String,
    val role: GroupRole
)